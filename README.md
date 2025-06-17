# My SkillsBuilder Journey
## Overview
This repo has copies of code from a group university project in Semester 2 of 2025. We were tasked with creating a gamified web app which would, in theory, be integrated with IBM SkillsBuild. However, we did not have access to an API, so it only simulates integration with SkillsBuild on a very basic level. You can view our full feature documentation [here](documentation/user-manual.pdf).

## Contributions
I have excluded scripts from the project that are not relevant to my direct contribution, which means the code in this repo is written by or heavily influenced by me. 

Below I have outlined information about my contribution on individual sections in this repo:
### Documentation
- [README.md](documentation/README.md): Full ownership. Due to the nature and depth of this project, none of the team had much experience with much of the technology used, so I researched and documented things which people may need to refer to during development. This also helped with project consistency, especially with our API. Since this project, I have grown in experience with REST API so would have aimed to structure things more conventionally.
- [user-manual.pdf](documentation/user-manual.pdf): For reference. This was added to by all members as they were completing user stories.

### Java (by package):
- [annotation](java/annotation): Full ownership
- [common](java/common): Full ownership
- [controller](java/controller): Some segments of code irrelevant to my contribution have been removed.
- [dto](java/dto): For reference.
- [model](java/model): Full ownership of everything except User, Course and Enrolment.
- [service](java/service): Full ownership of everything except UserService. Some segments of code irrelevant to my contribution have been removed.
- [validation](java/validation): Full ownership.

### CSS:
I did not take the lead on styling decisions for the vast majority of the project, but did write the CSS for the features I was responsible for. I only included the files for which I have majority ownership, and have included additional information below:
- [avatar-grid.css](resources/css/avatar-grid.css): Full ownership of code
- [submenu.css](resources/css/submenu.css): Used to be 4 separate files which was mostly others' work, which I combined and reworked into one.

I have gained more experience in CSS styling since completing this project, so would have structured it differently if I were to do it again.

### JS:
Although I spent a lot of time doing collaborative problem-solving with JavaScript, I have only included [interactive-learning-path.js](resources/js/interactive-learning-path.js), which I can claim full ownership of.

### Thymeleaf Templates:
These are the result of many iterations from several collaborators, so I cannot claim claim full ownership of any. Below are some more details:
- I have majority ownership of [avatar.html](templates/avatar.html) and [components.html](templates/components.html).
- I integrated my interactive learning tree features into [dashboard.html](templates/dashboard.html).
- I improved security and added validation to [login.html](templates/login.html), [register.html](templates/register.html), [reset-password.html](templates/reset-password.html) and [settings.html](templates/settings.html).
