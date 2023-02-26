
<!-- markdown-math = off -->

## How to

* Use Java 11 / mvn
* Set the environment variable TELLUR_HOME.
* Build the project by running 'buildToExecute.sh' and outputting the results to TELLUR_HOME
* Run the Demo Testplan by executing: 
```
java -jar ${TELLUR_HOME}/tellur-1.0.0.jar anConsealedArgParam "file:${TELLUR_HOME}/demo.html"
```

If you run the demo.wtl file successfully, you should see the following test results:

```
TC_001		passed  
TC_002		passed  
TC_003		passed  
TC_004		passed  
TC_005		passed  
TC_006_failedEx	failed	unAssigned_Test not defined 
TC_007		passed  
TC_008_failedEx	failed	URL not match - Expected: <anySite.html> but was file:${TELLUR_HOME}/demo.html  
```

* Create your own WTL Testplan
* Configure ${TELLUR_HOME}/config/application.properties to run your own Testplan afterwards


## Purpose

The purpose of the Web Testing Language (WTL) is to automate web testing. It is designed for creating automated test cases and testing web applications. The language provides a set of commands and functions that enable developers to interact with web elements such as buttons, labels, and input fields. The language allows users to define and include web elements, initialize and declare variables, define test cases, navigate to URLs, verify assumptions of the testing state, click on web elements, send text to input fields, and print output.

The main objective of the programming language is to simplify and streamline the process of web testing, by automating repetitive tasks and providing a consistent and reliable way of verifying the functionality of web applications. It enables developers to test web applications in a faster, more efficient, and systematic way, by reducing human errors and increasing the accuracy and reliability of test results.

Moreover, the programming language is designed to be easy to use and understand, even for those without extensive programming experience. Its syntax is simple and straightforward, and it is based on a set of easy-to-use commands and functions that can be combined to create complex test cases. This makes it accessible to a wide range of users, including developers, testers, and quality assurance professionals.

WTL is lightweight and easy to integrate into your continuous integration and continuous deployment (CI/CD) process. Additionally, its output is accessible and easy to understand, which makes postprocessing and analysis a breeze.


## WebElements

WebElements are the building blocks of web automation tests in WTL. They are defined in the program in Webdefinition-Files and correspond to elements on a web page. WebElements can be of three types: 
* label      A read-only element that displays text on a web page and can be checked for its output
* button     A clickable element that executes a specific action
* input      An element that accepts user input, such as text or numbers

WebElements can be defined and included in a program using the includeLabels, includeButtons, and includeInputs commands. These commands allow the program to access the properties and methods of these elements.

To interact with a WebElement, the program can use specific commands for each type of WebElement. For example, to verify the presence of a label or button, the program can use the verifyPresenceOf command. To set a value for an input element, the program can use the send command. To read a label, it can be assigned to a variable.

Overall, WebElements are essential to building automated tests in this programming language. They allow the program to interact with web pages in a consistent and reliable way, ensuring that the tests are repeatable and accurate.


## Commands

WTL provides various commands that can be used to interact with the webpage and verify its content. These commands can be divided into four categories: Navigation commands, Verification commands, Action commands, and Other commands.

A. Navigation commands: These commands are used to navigate to different web pages. The following navigation commands are available:
* navigateTo(url): Navigates to the specified URL.
* refresh(): Refreshes the current web page.

B. Verification commands: These commands are used to verify the presence and content of web elements. The following verification commands are available:
* verifyUrlContains(url): Verifies if the current URL contains the specified text.
* verifyPresenceOf(webElement): Verifies if the specified web element is present on the page.
* verifyLabelContains(label, text): Verifies if the specified label contains the specified text.
* verifyLabelContainsRex(label, regex): Verifies if the specified label matches the specified regular expression.

C. Action commands: These commands are used to interact with web elements. The following action commands are available:
* click(webElement): Clicks on the specified web element.
* send(inputElement, text): Sends the specified text to the specified input element.

D. Other commands: These commands are used for other purposes. The following other commands are available:
* var: Declares a global variable.
* sh(command): Calls the specified shell command and returns the output.
* sleep(seconds): Pauses the program for the specified number of seconds.
* set(variable, value): Sets the specified variable to the specified value.
* ret(value): Returns the specified value from a function.
* cat(values..): concatenates strings and numbers. Is a variadic function.

These commands provide a wide range of functionalities that can be used to interact with web pages, verify their content, and automate testing. By combining
these commands, complex test cases can be implemented and run automatically.


## Variables

In WTL variables are used to store and manipulate data. A variable is a named memory location that can hold a value of a specific data type, such as a string or a number.

Variables are always global and declared using the 'var' keyword followed by the variable name and an optional initial value.
Variables can be used in various parts of a program, such as in test cases, functions, or commands.

Function variables are a special type of variable that are used to pass arguments to a function. When a function is called, it can receive one or more arguments, which are stored in special variables named $$0, $$1 .. $$9.

Arg variables are another special type of variable that are passed from outside the program. Unlike other variables, arg variables are concealed in logs to ensure privacy, but can be used like any other variable within the testplan file. They can be accessed using the special variable names $$__arg_0, $$__arg_1 .. $$__arg_9. They are always from type string.
The values of variables can then be stored in other variables for use in subsequent calculations or operations.

Arg variables variables are automatically created by the programming language and are available only inside the function. They are used to access the arguments passed to the function by name rather than by position, which makes the code more readable and easier to understand.