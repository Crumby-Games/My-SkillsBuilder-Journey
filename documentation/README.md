# Group 56 CO2124 Project
## Configuration
The project is configured to use an MVC framework with Thymeleaf to handle the presentation layer (M**V**C), instead of using JSPs. Because of this, views are placed into .html files in the resources/templates folder (instead of webapp/WEB-INF), and data are accessed by them slightly differently than JSPs.

Spring Security is used. Currently, the SecurityConfig requires the user to be logged in for all templates, except for /login and /register, and access for static resources. /api cannot be used unless logged in and authenticated.

The sample database is configured to regenerate if empty, but not rewrite if there is already data.

## API Conventions
Generally, an /api/** url will signify that a request is data-orientated. The mapping should avoid @RequestParam where possible, and should instead specify details using @PathVariable. In cases where an id is passed for something other than the endpoint, the identifying entity needs to be included in the path, except for User (since all /api currently uses the current user as context). For example, with a 'quiz' endpoint, /api/course/{id}/quiz makes it clear that a quiz is identified by a course id.

/api GET requests should end with a noun to signify the context of the data. For example, /api/course returns data for a particular course. There are cases in which the /api can return more than just data for the entity itself. For example, /api/learning-path returns the data of all the courses within it as well as the learning path itself, since that is necessary to display a learning-path. They should return Map<String, Object> where possible, and if so, have the @JsonCompatible annotation to make this clear. It should not pass entities, but rather a combination of primitive data and collections.

/api POST requests are defined more loosely in this project. Some return values, some do not. However, they should always have a verb as the endpoint of their path and aim to make the action based off a particular entity (which could be abstract). For example /api/course/{id}/quiz.

## JavaScript Fetch Requests
To do GET requests in JavaScript, several asynchronous methods must be called. You can follow the following framework:
```js
fetch("/api/learning-path")
        .then(response => response.json())
        .then(data => {
            courseData = data['courses']
            // process data
        }).catch(error => {
            // error handling
        })
```

Because CSRF is enabled in this project, authentication tokens must be sent with POST requests. Typically, the token and its header is placed into the <head> of a Thymeleaf template with the following:
```html
<meta name="_csrf" th:content="${_csrf.token}" />
<meta name="_csrf_header" th:content="${_csrf.headerName}" />
```
You can access this in JavaScript using:
```js
const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
```
The convention is to place this at the global scope, but this must be done carefully as many templates have multiple JavaScript scripts that will have an overlapping Global scope.

The POST request itself can be done with the following framework:
```js
fetch(`/api/course/${courseId}/enrol`, {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken
    }
}).then(response => {
    if (response.ok) {
        // action succeeded
    }
});
```
If the POST request returns a string, you can process it using `.text()`, which is asynchronous, so you'd probably want to do something like this:
```js
fetch('..', {
}).then(response => {
    if (response.ok) {
        return response.text();
    }
}).then(text => {
    // Process text;
})
```

## General Thymeleaf Reference
Suppose a GET mapping is defined in a controller as following:
```java
@Controller
public class MainController {
    @GetMapping("/")
    public String start(Model model) {
        model.addAttribute("greeting", "Hello, world!");
        return "index"; // Refers to index.html in src/main/resources/templates/
    }
}
```
The string "Hello, world!" can be accessed as an attribute in the HTML by using the following:
```html
<p>Greeting: <span th:text="${greeting}">Greeting</span></p>!
```
This code would display: "Greeting: Hello, world!" in a <p> element, unless there some kind of error somewhere, in which case it might display "Greeting: Greeting" as the default. 

Note that th:text replaces the content of an HTML tag with a string of the data, so a <span> element is typically used for just the variable (as shown above). An alternative approach is concatenating the strings with java syntax inside the quotes, like so:
```html
<p th:text="'Greeting: ' + ${greeting}">Greeting</p>!
```

To access the current user in the controller, it must be allowed as an @AuthenticatedPrinciple parameter. To get the username of the current user, for example, you could do:
```java
@GetMapping("/")
public String start(Model model, @AuthenticatedPrinciple User user) {
    model.addAttribute("username", user.getName());
    return "index";
}
```
All paths to static resources in HTML (like stylesheets, images and JS scripts) are required to use the "th:href" attribute instead of href because they are dynamically generated by Thymeleaf. It should also use "@{path}" as the string where path is the relative path from resources/static. For example,
```html
<link rel="stylesheet" th:href="@{/css/main.css}">
```