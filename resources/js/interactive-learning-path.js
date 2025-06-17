// Gets learning path id as parameter from window
let currentLearningPathId =  new URLSearchParams(window.location.search).get("select");

if (currentLearningPathId != null) {
    (async () => {
        await loadGoJS();
        const $ = go.GraphObject.make;

        const avatar = await generateAvatar();
        const diagram = await generateDiagram();
        diagram.add(avatar.part);
        activateAvatar();
        setTimeout(() => {
            diagram.currentTool.doActivate();
        }, 500);
        async function loadGoJS() {
            if (!window.go) {  // Prevent reloading if already loaded
                await new Promise((resolve, reject) => {
                    const script = document.createElement("script");
                    script.src = "https://unpkg.com/gojs/release/go.js";
                    script.onload = () => {
                        resolve();
                    };
                    script.onerror = reject;
                    document.head.appendChild(script);
                });
            }
        }

        // Returns tree model diagram with all courses initialised
        async function generateDiagram() {
            const courseData = (await getDataFromMapping(`/api/learning-path/${currentLearningPathId}`))['courses'];
            courseData.forEach(formatCourseDataForTree);

            // Diagram config
            const diagram = $(go.Diagram, "learning-path", {
                initialContentAlignment: go.Spot.Center,
                padding: 100,
                layout: $(go.TreeLayout, {angle: 90, layerSpacing: 140}),
                "toolManager.mouseWheelBehavior": go.ToolManager.WheelZoom,
                isReadOnly: true,
                hasVerticalScrollbar: false,
                hasHorizontalScrollbar: false
            });

            // Node styling
            diagram.nodeTemplate =
                $(go.Node, "Auto",
                    {
                        selectable: false,
                        movable: false,
                        cursor: "pointer",
                        desiredSize: new go.Size(200, 200)
                    },
                    $(go.Shape, "RoundedRectangle", {
                        name: "SelectionOutline",
                        fill: null,
                        stroke: "#00669966",
                        strokeWidth: 5,
                        strokeDashArray: [20, 20],

                        visible: false
                    }),
                    $(go.Picture,
                        {
                            margin: 15,
                            imageStretch: go.GraphObject.Uniform,
                            stretch: go.GraphObject.Fill,
                        },
                        new go.Binding("source", "icon")),
                );

            // Arrow styling
            diagram.linkTemplate =
                $(go.Link,
                    {
                        selectable: false,
                        routing: go.Link.Orthogonal,
                        corner: 50,
                        toShortLength: 30,
                        toEndSegmentLength: 10,
                    },
                    $(go.Shape,
                        {stroke: "#006699", strokeWidth: 5, strokeDashArray: [20, 20]}
                    ),
                    $(go.Shape,
                        {toArrow: "Standard", stroke: "#006699", fill: "#006699", scale: 2,}
                    )
                );

            diagram.model = new go.TreeModel(courseData);

            return diagram;

            // go.TreeModel needs data in a particular format for generation
            function formatCourseDataForTree(courseData) {
                courseData['key'] = courseData['id'];
                delete courseData['id'];

                courseData['parent'] = courseData['prerequisite'] != null ? courseData['prerequisite'] : "";
                delete courseData['prerequisite'];

                delete courseData['learningPath'];
            }
        }

        // Returns an object which contains the following:
            // part: go.Part that is displayed on diagram
            // controller: allows current movement to be interrupted or paused independent of scope by modifying memory space
            // processPromise: promise of movement. Used to check if avatar 'is busy'.
            // spritePanel: shorthand access for panel inside part that separates each spritesheet onto a different layer
        async function generateAvatar() {
            const avatarFields = {part: null, controller: {interrupt: false, paused: false}, processPromise: null, spritePanel: null}
            const avatarData = await getDataFromMapping("/api/avatar/spritesheets");

            let spriteArray = [];
            for (let key in avatarData) {
                let sprite = avatarData[key];
                sprite.slot = key;
                spriteArray.push(sprite);
            }

            avatarFields.spritePanel = $(go.Panel, "Table");

            // Generates a go.Picture for each sprite and places it into spritePanel. The index of a sprite acts as its z-index.
            // For initialisation, the order of sprites is arbitrary. This is updated after loading.
            for (let i = 0; i < spriteArray.length; i++) {
                let sprite = spriteArray[i];
                avatarFields.spritePanel.add(
                    $(go.Picture, {
                        name: sprite.slot,
                        source: sprite.imagePath,
                        width: sprite.frameWidth,
                        height: sprite.frameHeight,
                        sourceRect: new go.Rect(0, 0, sprite.frameWidth, sprite.frameHeight)
                    })
                );
            }

            // The go object that is physically moved around to represent the avatar. Contains the spritePanel.
            avatarFields.part = $(go.Part, "Position",
                {
                    layerName: "Foreground",
                    pickable: false,
                    selectable: false,
                    location: new go.Point(),
                    visible: false,
                    locationSpot: go.Spot.Center
                },
                avatarFields.spritePanel
            );
            return avatarFields;
        }

        // Sets up triggers and contains behaviour for all avatar activity
        function activateAvatar() {
            let targetNode = null;
            let allowEncounter = true;

            // node1 and node2 refer to the nodes that the avatar are between during navigation. They are equal if the avatar is static.
            const currentEdge = {node1: null, node2: null}

            // Z indices for each layer in the spritePanel.
            let spriteZIndices = {character: 0}

            // Initialise navigation information immediately after tree is formed. Start with root node of tree selected.
            diagram.addDiagramListener("InitialLayoutCompleted", function (e) {
                const rootNode = diagram.findTreeRoots().first();
                if (rootNode) {
                    avatar.part.location = rootNode.actualBounds.center;
                    avatar.part.visible = true;
                    currentEdge.node1 = rootNode;
                    currentEdge.node2 = rootNode;
                    selectNode(rootNode);
                    processAvatarMovement([rootNode]);
                }
            });


            diagram.addDiagramListener("ObjectSingleClicked", async function (e) {
                const node = e.subject.part;

                // When the user clicks a node
                if (node instanceof go.Node) {

                    // If the node is not currently selected, select it and move avatar towards it.
                    if (targetNode !== node) {
                        selectNode(node);

                        // Arbitrarily select one of the adjacent nodes to traverse from
                        const path = findPath(currentEdge.node1, node);

                        // If the path makes use of both adjacent nodes, then it means it's backtracking.
                        if (path.length >= 2 && currentEdge.node2 === path[1]) {
                            // Remove the first node to ensure the shortest route.
                            path.shift()
                        }

                        // If the avatar is currently moving, politely ask it to stop before taking over for next movement action.
                        // Acts similar to a process lock.
                        if (avatar.controller) {
                            avatar.controller.interrupt = true;
                            await avatar.processPromise;
                        }

                        // Since this is a new movement action, reset controller
                        avatar.controller.interrupt = false;
                        avatar.controller.paused = false;

                        // Store this movement action
                        avatar.processPromise = processAvatarMovement(path);
                        await avatar.processPromise;

                    }
                    // If the node is already selected and the avatar is static, then open the course popup instead.
                    else if (currentEdge.node1 === currentEdge.node2) {
                        openCoursePopup(node.part.data.key);
                    }
                }
            });

            // Close modal and resume movement when the quiz modal close button is pressed
            document.querySelector(".close").addEventListener("click", function () {
                const modal = document.getElementById("quiz-modal");
                modal.classList.remove('show');

                // Allow time for transition
                setTimeout(() => {
                    if (!modal.classList.contains('show')) {
                        modal.style.display = 'none';
                    }
                }, 300);

                // Resume movement
                avatar.controller.paused = false;
            });

            // Update currently selected node by highlighting it on the diagram and update navigation data
            function selectNode(node) {
                if (node !== targetNode) {
                    loadLeaderboard(node.part.data.key); //Load leaderboard for selected node
                    allowEncounter = true;

                    if (targetNode != null) {
                        targetNode.isCurrent = false;
                        targetNode.findObject("SelectionOutline").visible = false;
                    }

                    targetNode = node;
                    node.isCurrent = true;

                    node.findObject("SelectionOutline").visible = true;
                    node.findObject("SelectionOutline").stroke = "#00669966";
                }
            }

            // Generate the shortest route between two nodes.
            // Since the diagram is a tree, the shortest route is always formed as a line, or a V. Below is the logical breakdown:
            /*
                branch1 = the full sequence of nodes from the start node and moving up until the root node
                Check if the target node is on branch1.
                If so, return the nodes of branch1 until target node.

                branch2 = the full sequence of nodes from the target node and moving up until the root node
                Check if the start node is on branch2.
                If so, return the reverse of the nodes of branch2 before start node.

                Find the intersection of branch1 and branch2.

                branch1 = all branch1 nodes before intersection
                branch2 = all branch2 nodes before intersection

                Return branch1 + reverse of branch2
            */
            function findPath(startNode, targetNode) {
                let branch1 = getBranch(startNode);
                if (branch1.includes(targetNode)) return spliceByNode(branch1, targetNode, true);
                let branch2 = getBranch(targetNode);
                if (branch2.includes(startNode)) return spliceByNode(branch2, startNode, true).toReversed();

                for (const node of branch1) {
                    if (branch2.includes(node)) {
                        spliceByNode(branch1, node);
                        spliceByNode(branch2, node);
                        branch2.pop();
                        break;
                    }
                }

                return branch1.concat(branch2.toReversed());

                // Starting with node, returns a list of nodes traversing upwards until it reaches the root
                function getBranch(node) {
                    const output = []
                    while (node !== null) {
                        output.push(node);
                        node = node.findTreeParentNode();
                    }
                    return output;
                }

                // Returns the list of nodes before/until a certain node in a branch (list of nodes).
                function spliceByNode(branch, node, inclusive = true) {
                    branch.splice(branch.indexOf(node) + (inclusive ? 1 : 0));
                    return branch;
                }
            }

            // Process properties
            let velocity = new go.Point(0, 0);

            // Even though the displayed direction of the avatar is discrete, its rotation angle is interpolated and is continuous.
            let facingAngle = 0;

            // Can also be stored to as the x and y frames of a spritesheet, where action is horizontal and direction is vertical.
            let frame = {action: 0, direction: 0}

            // Handles continual smooth movement during a traversal
            async function processAvatarMovement(path) {
                let step = 0;
                let node;

                // Travels through path until interrupted
                while (path.length > 0) {
                    // Ends movement entirely if it was interrupted by the controller or if it ran out of path early
                    if (checkForInterrupt() === 1 || checkForInterrupt() === 3) {
                        endTraversal();
                        return;
                    }

                    node = path.shift()

                    // If reaching last node, slow down before stopping and stop as close to target as possible
                    if (path.length === 0) {
                        await goToNode(node, true, false);
                    } else {
                        await goToNode(node, false, true);
                    }
                }

                // When it has reached the end, update a course to demonstrate that the avatar has arrived
                endTraversal();
                node.findObject("SelectionOutline").stroke = "#006699";

                // Updates navigation state after no longer moving
                function endTraversal() {
                    avatar.processPromise = null;
                    animIdle();
                    updateSprite();
                }

                // Continually checks until movement is no longer paused. Since the timing does not need to be precise, it only makes the check twice a second.
                async function waitForUnpause() {
                    while (avatar.controller.paused) {
                        await new Promise(resolve => setTimeout(resolve, 500));
                    }
                }

                // Returns a simplified code to signify reason for stopping
                function checkForInterrupt() {
                    if (avatar.controller.interrupt) {
                        return 3;
                    } else if (avatar.controller.paused) {
                        return 2;
                    } else if (path.length === 0) {
                        return 1;
                    }
                    return 0;
                }

                // Go to next node from current node. Must be adjacent.
                //      lazyRouting allows changing direction before fully reaching a bend. This stops movement looking robotic.
                //      easeOut is used for when the avatar needs to slow down as it reaches a node. This is used at the end of a path.
                async function goToNode(node, easeOut, lazyRouting) {
                    // Treat node2 as the target. If node1 is coincidentally the target, swap them.
                    if (node === currentEdge.node1) {
                        currentEdge.node1 = currentEdge.node2;
                    }
                    currentEdge.node2 = node;
                    let interrupt;

                    // One call of interpolateAvatarPosition is enough to get the avatar to its target node.
                    // However, this handles any number of pauses in the middle.
                    do {
                        await interpolateAvatarPosition(node.actualBounds.center, easeOut, lazyRouting);
                        interrupt = checkForInterrupt();
                        await waitForUnpause();
                    } while (interrupt === 2)

                    // Only update current node position to destination if there wasn't an early traversal cancellation.
                    if (interrupt !== 3) {
                        currentEdge.node1 = node;
                        currentEdge.node2 = node;
                    }
                }

                // Handles avatar movement to a position on the x and y axes
                function interpolateAvatarPosition(targetLocation, easeOut = false, lazyRouting = false) {
                    // Configuration
                    const maxSpeed = 5.;
                    const acceleration = 0.7;
                    const lazyStopDistance = 80;
                    const quizEncounterChance = 0.1;

                    // Prevention for rolling a chance for an encounter multiple times in the same movement
                    let checkedForEncounter = false;

                    // Distance between avatar's current position and destination
                    const totalDistance = vecLength(vecSubtract(targetLocation, avatar.part.location));

                    // Smoothly accelerates and continues moving and changing direction until it minimises the distance to its destination
                    // Also ends if interrupted by a pause
                    return new Promise((resolve) => {
                        // The avatar has some margin to treat its location as the destination. If lazy routing is enabled, more distance is allowed.
                        const earlyStopDistance = lazyRouting ? 5 + lazyStopDistance : 5;

                        // Distance from destination before destination is applied
                        let earlySlowDistance = 0;

                        if (easeOut) {
                            // The relative time that the avatar needs to slow down
                            const t = maxSpeed / acceleration;

                            // Equation for distance that the avatar needs to slow down with constant acceleration
                            earlySlowDistance = (maxSpeed * t + (-acceleration / 2) * t ** 2);

                            // Take destination margin loosely into account
                            earlySlowDistance += earlyStopDistance * 2;
                        }

                        // Notify diagram that movement is happening
                        diagram.startTransaction("Move Avatar");

                        // Start movement
                        animateStep();

                        // Physically move avatar and perform runtime calculations
                        function animateStep() {
                            const difference = vecSubtract(targetLocation, avatar.part.location);
                            const remainingDistance = vecLength(difference);
                            const speed = vecLength(velocity);

                            // Halfway to destination, there's a one-time chance for quiz encounter if doing so is viable and has not been done already.
                            if (!checkedForEncounter && allowEncounter && remainingDistance < totalDistance / 2) {
                                checkedForEncounter = true;
                                allowEncounter = false;
                                if (Math.random() > 1 - quizEncounterChance) {
                                    showRandomQuiz()
                                }
                            }

                            // If controller has paused, interrupted or if the avatar has reached its destination, conclude movement.
                            if (avatar.controller.paused && speed === 1. || avatar.controller.interrupt || remainingDistance <= earlyStopDistance) {
                                // Set velocity to 0 unless it is meant to preserved because of a traversal interruption
                                if (avatar.controller.paused || easeOut && !avatar.controller.interrupt) {
                                    velocity = new go.Point(0, 0);
                                }
                                diagram.commitTransaction("Move Avatar");
                                resolve();
                                return;
                            }

                            // Physically update avatar location by velocity
                            avatar.part.location = vecAdd(avatar.part.location, velocity);

                            // Update sprite only every 4 animation steps for performance
                            if (step % 4 === 0) {
                                updateSprite();
                            }

                            // Update walk cycle frame
                            animWalk(step, speed / maxSpeed);

                            // Interpolates facingAngle and updates frame accordingly.
                            const targetDirection = vecNormalise(difference);
                            const velocityDirection = vecNormalise(velocity);
                            facingAngle = angleLerp(facingAngle, vecAngleDegrees(velocityDirection), 0.15);
                            animLookDirection(angleDirectionStep(facingAngle));

                            // Decelerating
                            if (avatar.controller.paused || (easeOut && remainingDistance <= earlySlowDistance)) {
                                // Prevent negative velocities by slowing down too much
                                if (speed > Math.max(1., acceleration)) {
                                    velocity = vecSubtract(velocity, vecScale(velocityDirection, acceleration));
                                } else {
                                    // Never go below 1 speed for edge cases where earlySlowDistance is too high
                                    velocity = vecScale(velocityDirection, 1);
                                }
                            }

                            // Accelerating/Maintaining speed
                            else {
                                velocity = vecAdd(velocity, vecScale(targetDirection, acceleration));
                                let newSpeed = vecLength(velocity);
                                // Cap speed to maxSpeed
                                if (newSpeed > maxSpeed) {
                                    velocity = vecScale(vecNormalise(velocity), maxSpeed);
                                }
                            }

                            // Continue to next animation step
                            step++;
                            requestAnimationFrame(animateStep);
                        }
                    })

                    /* Vector operations using go.Point() as a simple form for data */

                    function vecAdd(vec1, vec2) {
                        return new go.Point(vec1.x + vec2.x, vec1.y + vec2.y);
                    }

                    function vecSubtract(vec1, vec2) {
                        return new go.Point(vec1.x - vec2.x, vec1.y - vec2.y);
                    }

                    function vecScale(vec, scale) {
                        return new go.Point(vec.x * scale, vec.y * scale);
                    }

                    function vecNormalise(vec) {
                        let length = vecLength(vec);
                        if (length === 0) return new go.Point(0, 0);
                        return vecScale(vec, 1 / length);
                    }

                    function vecLength(vec) {
                        return Math.sqrt(vec.x ** 2 + vec.y ** 2);
                    }

                    // Finds angle between up vector and given vector in degrees.
                    function vecAngleDegrees(vec) {
                        // Invert Y because screen coordinates are flipped
                        return (Math.atan2(vec.y, vec.x) * (180 / Math.PI) + 360) % 360;
                    }

                    // Linearly interpolates an angle in degrees, allowing wrapping around 360. Returns a value between 0 and 360.
                    function angleLerp(startAngle, endAngle, weight) {
                        startAngle = (startAngle + 360) % 360;
                        endAngle = (endAngle + 360) % 360;

                        let difference = endAngle - startAngle;
                        if (difference > 180) {
                            difference -= 360;
                        } else if (difference < -180) {
                            difference += 360;
                        }

                        return (startAngle + difference * weight + 360) % 360;
                    }

                    // Converts an angle in degrees to a discrete 45 degree "direction step", used in sprite animations
                    function angleDirectionStep(angle) {
                        return Math.round(angle / 45) % 8;
                    }
                }

                // Updates the avatar on diagram based on animation frames and direction
                function updateSprite() {
                    avatar.spritePanel.elements.each(function (sprite) {
                        // Update spritesheet frame to consider action state and direction
                        sprite.sourceRect = new go.Rect(
                            frame.action * sprite.width,
                            frame.direction * sprite.height,
                            sprite.width,
                            sprite.height
                        );

                        // Update z-index data based on direction
                        //      Hardcoded since all equipment for a given slot will behave the same, in terms of z indexes
                        //      Relative to character which always has z index 0.
                        switch (sprite.name) {
                            case "rh":
                                spriteZIndices[sprite.name] = frame.direction <= 1 || frame.direction >= 6 ? 1 : -1
                                break;
                            case "lh":
                                spriteZIndices[sprite.name] = frame.direction >= 1 && frame.direction <= 3 ? 1 : -1
                                break;
                        }
                    })

                    sortLayers()

                    // Using z-indices as a sorting key for each sprite, updates the sequence in spritesPanel to sort layers
                    function sortLayers() {
                        let layers = [];
                        avatar.spritePanel.elements.each(function(layer) {
                            layers.push({ layer: layer, slot: layer.name });
                        });

                        layers.sort(function(a, b) {
                            return spriteZIndices[a.slot] - spriteZIndices[b.slot];
                        });

                        layers = layers.map(function(item) { return item.layer; })

                        avatar.spritePanel.elements.each(function(layer) {
                            avatar.spritePanel.remove(layer);
                        });
                        for (let sprite of layers) {
                            avatar.spritePanel.add(sprite);
                        }
                    }
                }

                // Updates frame for walk cycle, using animation step as reference. Scalable speed.
                function animWalk(step, speed) {
                    if (frame.action === 0) {
                        frame.action = 1;
                    } else {
                        frame.action = Math.round(step * speed * 0.1) % 2 + 1;
                    }
                }

                // Updates frame for idling. Since there is no animation for idling currently, this simply resets the action frame.
                function animIdle() {
                    frame.action = 0;
                }

                // Updates frame for the direction of sprites
                function animLookDirection(direction) {
                    frame.direction = (direction + 7) % 8;
                }
            }
        }

        // Get and reveal random quiz encounter
        function showRandomQuiz() {
            fetch(`/api/learning-path/${currentLearningPathId}/random-quiz`)
                .then(response => response.json())
                .then(data => {
                    if (Object.keys(data).length === 0) {
                        return;
                    }
                    avatar.controller.paused = true;

                    document.getElementById("quiz-title").innerText = `${data.courseName}`;
                    document.getElementById("quiz-message").innerText = data.isHighLevel
                        ? "⚠️ This is a high-level quiz encounter!"
                        : "Would you like to proceed?";
                    const modal = document.getElementById("quiz-modal");
                    modal.style.display = 'flex';

                    setTimeout(() => {
                        if (!modal.classList.contains('show')) {
                            modal.classList.add('show');
                        }
                    }, 100);

                    document.getElementById("quiz-start-btn").onclick = function () {
                        window.location.href = `/quiz?courseId=${data.course}`;
                    };
                })
                .catch(error => console.error('Error fetching quiz:', error));
        }

    })();
}

// Generic shorthand for getting JSON data using the API.
function getDataFromMapping(mapping) {
    return fetch(mapping)
        .then(response => {
            if (!response.ok) {
                throw new Error("HTTP error at " + mapping + "\nStatus: ${response.status}");
            }
            return response.json();
        })
        .catch(error => console.error("Error fetching data:", error));
}

// Updates current leaderboard to show currently selected node.
function loadLeaderboard(courseId) {
    const container = document.getElementById("leaderboard-container");
    container.innerHTML = "<br>Loading leaderboard...";

    fetch(`/leaderboard?courseId=${courseId}`)
        .then(res => {
            if (!res.ok) throw new Error("Failed to load leaderboard.");
            return res.text();
        })
        .then(html => {
            container.innerHTML = html;
            const row = document.getElementById("current-user-row");

            if (row && container) {
                container.scrollTop = row.offsetTop - container.offsetTop;
            }
        })
        .catch(err => {
            container.innerHTML = "Error loading leaderboard.";
            console.error(err);
        });
}

