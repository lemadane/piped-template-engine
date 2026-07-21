# Piped Template Engine (PTE)

[![JitPack](https://jitpack.io/v/lemadane/piped-template-engine.svg)](https://jitpack.io/#lemadane/piped-template-engine)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)

**Piped Template Engine (PTE)** is an ultra-fast, beginner-friendly HTML template engine for Java and Spring Boot. It lets you write clean HTML templates using simple pipes (`|variable|`) instead of complex tags.

```html
<h1>|title|</h1>
<p>Hello, |user?.displayName ?? 'Guest'|</p>
```

---

## ⚡ Why Use Piped Template Engine?

- **🐣 Simple & Readable**: Write variables as `|name|` instead of complex syntax.
- **🚀 Blazing Fast**: Compiles templates directly in RAM for native Java execution speed.
- **📁 File-Based Routing**: Works like SvelteKit—just drop a `+page.pte` file into a folder to create a web route!
- **🔁 Loop Separators**: Built-in `|separator|` directive for clean lists and breadcrumbs without repetitive `if (!last)` checks.
- **🍃 Spring Boot Ready**: Seamless integration with Spring MVC.
- **⚡ HTMX Friendly**: Perfect for modern, dynamic single-page interactions without heavy JavaScript.
- **🛡️ Safe**: Automatically prevents XSS security vulnerabilities by default.

---

## 📦 1. Installation

Add **JitPack** to your `build.gradle` file:

```gradle
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    // For Spring Boot applications:
    implementation 'com.github.lemadane.piped-template-engine:piped-template-engine-spring-boot-starter:v0.1.1'
}
```

---

## 📁 2. File-Based Routing (SvelteKit-Style)

You don't need to write Java controller classes just to display HTML pages! Just put your templates inside `src/main/resources/pte-routes/`:

### Folder Structure
```txt
src/main/resources/pte-routes/
├── +page.pte              <-- Automatically creates route: GET /
├── about/
│   └── +page.pte          <-- Automatically creates route: GET /about
└── tasks/
    ├── +page.pte          <-- Automatically creates route: GET /tasks
    └── [id]/
        └── +page.pte      <-- Automatically creates route: GET /tasks/{id}
```

### Example: Dynamic Page (`routes/tasks/[id]/+page.pte`)
The `[id]` parameter is automatically available in your template:

```html
<div class="card">
    <h1>Task ID: |id|</h1>
</div>
```

---

## 📖 3. Template Syntax Made Simple

### 1. Variables & Safe Formatting
```html
|user.name|                  <!-- Displays text safely -->
|html user.bio|              <!-- Allows trusted raw HTML -->
|title, slug|                <!-- Turns 'Hello World!' into 'hello-world' -->
|name, upper|                <!-- Turns 'john' into 'JOHN' -->
```

### 2. Strongly Typed Models & Form Field Binding
```html
<!-- Strongly Typed Model Contract -->
|model com.example.model.CustomerEditPage|

<!-- Automatic Form Field Binding (generates name, id, value, and validation error classes) -->
<input |field model.form.email|>

<!-- Type-Based Display and Editor Widgets -->
|display model.customer.address|
|editor model.form.email|
```

### 3. Template-Defined Macros (Twig-Style)
Define reusable HTML helper macros and invoke them anywhere:

```html
<!-- Define a Macro -->
|macro input(name, value, type)|
    <input type="|type ?? 'text'|" name="|name|" value="|value|" class="input">
|/macro|

<!-- Call a Macro -->
|call input('username', user.name)|
|call input('email', user.email, 'email')|
```

### 2. Default Values & Optional Chaining
```html
<!-- If user.profile is missing, fallback to 'Guest' -->
<p>Welcome, |user?.profile?.name ?? 'Guest'|</p>

<!-- Ternary IF/ELSE inside attributes -->
<span class="|completed ? 'done' : 'pending'|">Status</span>
```

### 3. If / Else Conditionals
```html
|if user.loggedIn|
    <p>Welcome back, |user.name|!</p>
|else|
    <p>Please log in.</p>
|/if|
```

### 4. Loops & Separators
```html
<!-- Comma-Separated List with |separator| (Renders between items except the last) -->
|each tag in tags||tag||separator|, |/separator||/each|

<!-- HTML List with Dividers -->
|each item in items|
    <span>|item.title|</span>
    |separator|
        <span class="divider">/</span>
    |/separator|
|else|
    <p>No items found.</p>
|/each|
```

### 5. Layouts (Reusing Headers & Footers)

`layouts/main.pte` (The Master Template):
```html
<!DOCTYPE html>
<html>
<head>
    <title>|yield title|</title>
</head>
<body>
    <main>|yield content|</main>
</body>
</html>
```

`pages/index.pte` (The Page Template):
```html
|layout layouts/main|

|section title| My Website |/section|

|section content|
    <h1>Hello World!</h1>
|/section|
```

---

## 🍃 4. Standard Spring Boot Controllers

You can also use standard `@Controller` annotations if you prefer:

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Welcome Page");
        return "pages/index"; // Opens src/main/resources/pte-templates/pages/index.pte
    }
}
```

---

## 🎨 5. Demo Application (TaskMaster)

This repository includes `task-master`, a complete sample TODO app built with Spring Boot, Bulma CSS, Material Design, and HTMX.

To run the demo app locally:
```bash
./gradlew :task-master:bootRun
```
Then open your browser to `http://localhost:8080`.

---

## 📄 License

This project is licensed under the **MIT License**.