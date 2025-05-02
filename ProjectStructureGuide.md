# FocusFlow Project Structure Guide

## Introduction

This guide will help you understand how our project is organized and where to put new files you create. 

## Why Structure Matters

As our app grows bigger than the simple programs we have made in class, good organization becomes extremly important. Without it, finding files becomes difficult, and the code can turn into "spaghetti" where everything depends on everything else.

Our structure follows a common pattern in professional software development that separates different types of code based on what they do. This makes it easier to:
- Find what you're looking for
- Change one part without breaking others
- Work together as a team

## The Big Picture: Maven Project Structure

Our project uses Maven, which is a tool that helps manage dependencies (libraries we use) and build our application. Maven projects follow a standard folder structure:

```
focusflow/
├── src/
│   ├── main/
│   │   ├── java/          # All our Java code goes here
│   │   └── resources/     # Non-code files (images, sounds, etc.)
│   └── test/              # Our test code
└── pom.xml                # Maven configuration file
```

Inside the `java` folder, we have our own organization system with these main folders:

```
com.teamdijkstra.focusflow/
├── application/
├── domain/
├── infrastructure/
├── service/
├── ui/
└── util/
```

Let's look at what goes in each one!

## Where to Put Your Classes

### Application Folder

**What it's for:** Classes that control and coordinate the whole application

**Put a class here if:** It's responsible for starting the app, managing the high-level flow, or connecting different major parts together.

**Examples:**
- `FocusFlowApp.java` - The main class that starts everything
- `ApplicationController.java` - Coordinates between all the different services
- `UIManager.java` - Manages all the screens and windows

Think of these classes as the "conductors" of our application!

### Domain Folder

**What it's for:** Classes that represent the core "things" in our application

**Put a class here if:** It represents a key concept in our app that users think about, like tasks, users, or timers.

**Examples:**
- `Task.java` - Represents a task the user creates
- `User.java` - Represents information about a user
- `TimerSession.java` - Represents a focus session

These classes should focus on properties and simple behaviors. They shouldn't worry about how data is saved or how the user interface looks.

### Service Folder

**What it's for:** Classes that implement the main functionality of our app

**Put a class here if:** It provides a specific set of related operations, especially if those operations work with domain objects.

**Examples:**
- `TaskManagementServiceImpl.java` - Handles all the operations for managing tasks
- `TimerServiceImpl.java` - Controls the timer functionality
- `NotificationServiceImpl.java` - Manages notifications

If you're writing code that implements business logic (like "what happens when a task is completed" or "how does the timer work"), it probably belongs in a service.

### Infrastructure Folder

**What it's for:** Classes that handle technical details like saving data

**Put a class here if:** It deals with "plumbing" like storing/retrieving data, connecting to external systems, or managing configurations.

**Examples:**
- `TaskRepositoryImpl.java` - Handles saving and loading tasks
- `DataSerializer.java` - Converts objects to formats that can be saved
- `ConfigurationService.java` - Manages app settings

These classes handle the behind-the-scenes technical work that supports our application.

### UI Folder

**What it's for:** Classes that create what the user sees and interacts with

**Put a class here if:** It displays information to the user or handles user inputs.

**Examples:**
- `MainDashboard.java` - The main screen of our app
- `TaskListView.java` - Shows the list of tasks
- `TimerDisplay.java` - Shows the timer
- `SettingsView.java` - The settings screen

UI classes should only worry about display and user interaction. They should call service classes to do the actual work.

### Util Folder

**What it's for:** Helper classes with common functionality used throughout the app

**Put a class here if:** It provides general utility methods that might be used by many different parts of the application.

**Examples:**
- `DateTimeUtils.java` - Helper methods for working with dates and times
- `ValidationUtils.java` - Methods for validating data
- `FileUtils.java` - Helper methods for working with files

## A Simple Decision Guide

If you're creating a new class and aren't sure where it belongs, ask yourself these questions:

1. Does it represent a core concept in our app (like a task or user)? → **domain** folder
2. Does it implement business logic or operations? → **service** folder
3. Does it show something to the user or handle user input? → **ui** folder
4. Does it handle saving/loading data or technical "plumbing"? → **infrastructure** folder
5. Does it coordinate between multiple major parts of the app? → **application** folder
6. Is it a helper with general methods used in multiple places? → **util** folder

## Interfaces and Implementations

Many of our service classes come in pairs:
- An interface (like `ITaskManagementService.java`) that defines what operations are available
- An implementation (like `TaskManagementServiceImpl.java`) that actually performs those operations

The interface should go in the main service folder, while the implementation goes in a subfolder matching its name (like `service/task/`).

## Naming Conventions

- Interface names should start with "I" (e.g., `ITaskService`)
- Implementation classes should end with "Impl" (e.g., `TaskServiceImpl`)
- Enums (special classes with fixed sets of values) should be named for what they represent (e.g., `TaskStatus`)
- Factory classes that create objects should end with "Factory" (e.g., `TaskFactory`)

## Still Not Sure?

If you're still not sure where to put a new class you're creating, just ask! It's better to ask than to put it in the wrong place and cause confusion later.

## Need More Help?

Remember, you can always look at existing similar classes to see where they're placed. This can give you good hints about where your new class belongs.

Happy coding, Team FocusFlow! 🚀
