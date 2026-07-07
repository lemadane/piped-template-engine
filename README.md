# Piped Template Engine

**Piped Template Engine**, or **PTE**, is a lightweight Java server-side template engine that uses readable pipe-based syntax for HTML rendering.

```html
<h1>|title|</h1>
<p>Hello, |user?.profile?.displayName ?? 'Guest'|</p>
```

PTE is designed to be simple, safe by default, HTML-first, framework-independent, and friendly to MVC and HTMX-style applications.

---

## Project Status

PTE is currently in active early development.

Implemented and covered:

```txt
(done) String rendering
(done) File-based page rendering
(done) Partial rendering
(done) HTML-escaped output
(done) Trusted HTML output
(done) Attribute-safe output
(done) URL-safe output
(done) JSON-safe output
(done) Comments
(done) If / else-if / else conditionals
(done) Truthy/falsy evaluation
(done) Boolean operators
(done) Comparison operators
(done) Each loops
(done) Map loops
(done) Each metadata
(done) Switch blocks
(done) Explicit switch fallthrough
(done) Optional chaining
(done) Null coalescing
(done) Ternary operator
(done) Includes and partials
(done) Circular include detection
(done) Layouts, sections, and yield
(done) Components and named slots
(done) Comma filters
(done) Date and time filters
```

Still planned:

```txt
(pending) Automatic whitespace cleanup
(pending) Better error messages with line/column
(pending) Template caching
(pending) Custom user-defined filters
(pending) Builder/config API
(pending) Optional .piped.html extension support
(pending) Spring Boot MVC integration
(pending) HTMX controller demo
(pending) Unit and integration test coverage
(pending) Internal tokenizer/parser/AST refactor
```

---

## Requirements

```txt
Java 17 or higher
Gradle
JUnit 5
```

PTE targets Java 17 as the minimum version while remaining compatible with newer Java versions.

---

## Quick Usage

### Render a string template

```java
TemplateEngine engine = new TemplateEngine();

String html = engine.renderString(
      "<h1>|title|</h1>",
      Map.of("title", "Dashboard")
);

System.out.println(html);
```

Output:

```html
<h1>Dashboard</h1>
```

### Render a file-based page

```java
TemplateEngine engine = new TemplateEngine(Path.of("src/main/piped"));

String html = engine.render("pages/products", model);
```

This loads:

```txt
src/main/piped/pages/products.piped
```

### Render a partial

```java
TemplateEngine engine = new TemplateEngine(Path.of("src/main/piped"));

String html = engine.renderPartial("partials/product-card", product);
```

This loads:

```txt
src/main/piped/partials/product-card.piped
```

This is useful for HTMX-style responses.

---

## Recommended Template Structure

```txt
src/main/piped/
  layouts/
    main.piped
  pages/
    products.piped
  partials/
    header.piped
    footer.piped
    product-card.piped
  components/
    card.piped
```

---

## Output Modes

### HTML-escaped output

```html
<h1>|title|</h1>
<p>|comment|</p>
```

`|expr|` evaluates an expression and escapes HTML.

If `comment` contains:

```html
<strong>Hello</strong>
```

The rendered output becomes:

```html
&lt;strong&gt;Hello&lt;/strong&gt;
```

### Trusted HTML output

```html
<article>
   |html post.bodyHtml|
</article>
```

`|html expr|` renders trusted HTML without escaping.

Use this only for trusted or sanitized HTML.

### Attribute-safe output

```html
<input value="|attr user?.profile?.displayName ?? 'Guest'|">
```

If the value is:

```txt
Juan "Boss" Dela Cruz
```

The rendered output becomes:

```html
<input value="Juan &quot;Boss&quot; Dela Cruz">
```

Use `|attr expr|` inside HTML attributes.

### URL-safe output

```html
<a href="/search?q=|url searchTerm|">Search</a>
```

If `searchTerm` is:

```txt
coffee & sugar
```

The rendered output becomes:

```html
<a href="/search?q=coffee+%26+sugar">Search</a>
```

### JSON-safe output

```html
<script>
   const product = |json product|;
</script>
```

Useful for JavaScript and AlpineJS:

```html
<div x-data='|json alpineData|'>
   Product data is available.
</div>
```

---

## Comments

Single-line comment:

```html
|# This comment will not be rendered. |
```

Block comment:

```html
|#
  This whole section is ignored.
  It can contain notes or disabled PTE syntax.
#|
```

---

## Expressions

### Property access

```html
|user.name|
|product.price|
|invoice.customer.name|
```

PTE can read values from:

```txt
Map keys
Java records
JavaBean getters
Boolean getters
Public fields
```

### Optional chaining

```html
|user?.profile?.displayName|
```

If `user`, `profile`, or `displayName` is missing, the expression returns nothing instead of throwing.

### Null coalescing

```html
|user?.profile?.displayName ?? 'Guest'|
```

The fallback is used when the left side is `null` or missing.

Example:

```html
|invoice?.customer?.companyName ?? invoice?.customer?.fullName ?? 'Walk-in Customer'|
```

### Ternary operator

```html
|user?.active ? 'Active' : 'Inactive'|
```

Ternary works with output modes:

```html
<button class="button |attr user?.active ? 'is-success' : 'is-danger'|">
   |user?.active ? 'Enabled' : 'Disabled'|
</button>
```

---

## Truthy and Falsy Rules

Falsy values:

```txt
null
missing value
false
0
""
"     "
empty List / Collection
empty Map
empty array
Optional.empty()
```

Truthy values:

```txt
true
non-zero number
non-blank string
non-empty List / Collection
non-empty Map
non-empty array
Optional.of(value), depending on the value
any other object
```

Example:

```html
|if products|
   <p>Products available.</p>
|else|
   <p>No products found.</p>
|/if|
```

---

## Conditionals

```html
|if user|
   <p>Hello, |user.name|</p>
|else|
   <p>Hello, guest</p>
|/if|
```

Official else-if syntax:

```html
|if user?.role == 'admin'|
   <p>Administrator</p>
|else-if user?.role == 'manager'|
   <p>Manager</p>
|else|
   <p>User</p>
|/if|
```

PTE uses `else-if`, not `elseif` or `else if`.

---

## Boolean Operators

PTE supports readable word-based boolean operators:

```txt
not
and
or
nand
nor
```

Examples:

```html
|if not user|
   <a href="/login">Login</a>
|/if|

|if user.active and user.verified|
   <p>Verified active user.</p>
|/if|

|if user.admin or user.manager|
   <a href="/dashboard">Dashboard</a>
|/if|
```

---

## Comparison Operators

```txt
==
!=
>
>=
<
<=
```

Examples:

```html
|if user.role == 'admin'|
   <a href="/admin">Admin Dashboard</a>
|/if|

|if product.stock > 0|
   <span>Available</span>
|else|
   <span>Sold out</span>
|/if|
```

---

## Loop Rendering

### Collection loop

```html
<ul>
   |each product in products|
      <li>|product.name|</li>
   |else|
      <li>No products found.</li>
   |/each|
</ul>
```

The optional `|else|` block renders when the collection is empty or missing.

### Map loop

```html
|each key, value in settings|
   <p>|key| = |value|</p>
|/each|
```

If output order matters, pass a `LinkedHashMap`.

---

## Each Metadata

Inside an `each` block, PTE exposes `each` metadata.

```html
|each.index|
|each.index0|
|each.first|
|each.last|
|each.even|
|each.odd|
```

Definitions:

```txt
each.index   = 1-based index
each.index0  = 0-based index
each.first   = true if current item is first
each.last    = true if current item is last
each.even    = true if 1-based index is even
each.odd     = true if 1-based index is odd
```

Example:

```html
|each product in products|
   <li class="|attr each.even ? 'even' : 'odd'|">
      |each.index|. |product.name|
   </li>
|/each|
```

---

## Switch Rendering

```html
|switch order.status|
   |case 'paid'|
      <span class="badge is-success">Paid</span>

   |case 'pending'|
      <span class="badge is-warning">Pending</span>

   |case 'cancelled'|
      <span class="badge is-danger">Cancelled</span>

   |default|
      <span class="badge is-muted">Unknown</span>
|/switch|
```

Switch cases do not fall through by default.

Use `|fallthrough|` for explicit fallthrough:

```html
|switch user.role|
   |case 'admin'|
      <a href="/admin">Admin Dashboard</a>
      |fallthrough|

   |case 'manager'|
      <a href="/reports">Reports</a>
      |fallthrough|

   |case 'user'|
      <a href="/account">My Account</a>
|/switch|
```

---

## Includes and Partials

### Include with current context

```html
|include partials/header|

<main>
   <h1>|title|</h1>
</main>

|include partials/footer|
```

### Include with custom context

```html
|each product in products|
   |include partials/product-card with product|
|/each|
```

`partials/product-card.piped`:

```html
<li class="product-card">
   <strong>|name|</strong>
   <span>₱|price|</span>
</li>
```

Because the partial is included `with product`, the partial can use `|name|` and `|price|`.

PTE detects circular includes.

Example error:

```txt
Circular include detected: partials/header -> partials/nav -> partials/header
```

---

## Layouts and Sections

Page template:

```html
|layout layouts/main|

|section title|
   Products - PTE Demo
|/section|

|section content|
   <h2>Products</h2>

   <ul>
      |each product in products|
         |include partials/product-card with product|
      |else|
         <li>No products found.</li>
      |/each|
   </ul>
|/section|
```

Layout template:

```html
<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8">
   <title>|yield title|</title>
</head>
<body>
   |include partials/header|

   <main>
      |yield content|
   </main>

   |include partials/footer|
</body>
</html>
```

Supported syntax:

```txt
|layout template-name|
|section section-name|
|/section|
|yield section-name|
```

Rules:

```txt
|layout| must be the first directive in the page.
A layout page can contain only section blocks after the layout directive.
|yield| is used inside layout templates.
```

---

## Components and Slots

Page template:

```html
|component components/card|
   |slot title|
      Product List
   |/slot|

   |slot body|
      <ul>
         |each product in products|
            |include partials/product-card with product|
         |else|
            <li>No products found.</li>
         |/each|
      </ul>
   |/slot|

   |slot actions|
      <a href="/products/new">Add Product</a>
   |/slot|
|/component|
```

Component template:

```html
<section class="card">
   <header class="card-header">
      <h3>|slot title|</h3>
   </header>

   <div class="card-body">
      |slot body|
   </div>

   <footer class="card-footer">
      |slot actions|
   </footer>
</section>
```

Supported syntax:

```txt
|component template-name|
|slot name| ... |/slot|   defines slot content
|slot name|               renders slot content inside a component
|/component|
```

Rules:

```txt
Only slot blocks are allowed directly inside a component block.
Slots can contain normal PTE syntax, including if, each, switch, and include.
Nested slot blocks are not allowed.
```

---

## Filters

PTE supports comma-based filters.

```html
|expression, filter|
|expression, filter arg|
|expression, filter1, filter2|
```

Examples:

```html
|user.name, upper|
|user.name, lower|
|messyText, trim, capitalize|
|user.nickname, default 'No nickname'|
|products, length|
|product.price, currency '₱'|
|largeNumber, number '#,##0.00'|
```

Filters work with output modes:

```html
<a href="/search?q=|url searchTerm, lower|">
   Search
</a>

<div class="user-|attr user.name, lower|">
   User class example
</div>
```

Built-in filters:

```txt
upper
lower
trim
capitalize
length
default
currency
number
date
time
datetime
```

---

## Date and Time Filters

Default formats:

```html
|createdAt, date|
|createdAt, time|
|createdAt, datetime|
```

Custom formats:

```html
|createdAt, date 'MMM dd, yyyy'|
|createdAt, time 'hh:mm a'|
|createdAt, datetime 'yyyy-MM-dd HH:mm:ss'|
```

Supported values:

```txt
Instant
LocalDate
LocalTime
LocalDateTime
java.util.Date
ISO date/time strings
```

Examples:

```html
<p>Created: |createdAt, datetime 'MMM dd, yyyy hh:mm a'|</p>
<p>Due date: |dueDate, date 'MMMM dd, yyyy'|</p>
```

For `Instant` and `java.util.Date`, PTE currently formats using the JVM default timezone.

---

## HTMX-Friendly Partial Rendering

PTE does not replace HTMX.

```txt
PTE  = renders HTML
HTMX = requests and swaps HTML
```

Example page template:

```html
<form
   hx-post="/products"
   hx-target="#products"
   hx-swap="beforeend"
>
   <input name="name" required>
   <button>Add Product</button>
</form>

<ul id="products">
   |each product in products|
      |include partials/product-card with product|
   |/each|
</ul>
```

Controller idea:

```java
Product product = productService.create(request);

String html = engine.renderPartial(
      "partials/product-card",
      product
);

return html;
```

---

## Official Syntax Summary

```txt
|expr|                         HTML-escaped output
|html expr|                    trusted HTML output
|attr expr|                    attribute-safe output
|url expr|                     URL-safe output
|json expr|                    JSON-safe output

|if expr|                      starts conditional block
|else-if expr|                 else-if branch
|else|                         else branch
|/if|                          closes if block

|each item in items|           loops over collection
|each key, value in map|       loops over map
|/each|                        closes each block

|switch expr|                  starts switch block
|case value|                   case branch
|default|                      default branch
|fallthrough|                  explicit switch fallthrough
|/switch|                      closes switch block

|include template|             includes template with current context
|include template with expr|   includes template with custom context

|layout template|              page layout directive
|section name|                 starts section block
|/section|                     closes section block
|yield name|                   renders section inside layout

|component template|           starts component block
|slot name| ... |/slot|        defines slot content
|slot name|                    renders slot content inside component
|/component|                   closes component block

|# comment |                   single-line comment
|# ... #|                      block comment

not                            boolean NOT
and                            boolean AND
or                             boolean OR
nand                           boolean NAND
nor                            boolean NOR
?.                             optional chaining
??                             null fallback
? :                            ternary operator
, filter                       filter pipeline
```

Avoided syntax:

```txt
|! expr|       not official
|< expr|       not official
||expr||       not official
|partial x|    not official; use |include x|
|elseif expr|  not official; use |else-if expr|
|else if expr| not official; use |else-if expr|
```

---

## Current Project Structure

Example structure:

```txt
piped-template-java/
  settings.gradle
  build.gradle
  README.md
  .gitignore
  src/
    main/
      java/
        com/
          piped/
            template/
              TemplateEngine.java
              Main.java
              escape/
              exceptions/
              expression/
              metadata/
              parsers/
              statements/
      piped/
        layouts/
          main.piped
        pages/
          products.piped
        partials/
          header.piped
          footer.piped
          product-card.piped
        components/
          card.piped
    test/
      java/
        com/
          piped/
            template/
```

---

## Testing

Run all tests:

```bash
./gradlew test
```

Run clean test:

```bash
./gradlew clean test
```

Run the demo:

```bash
./gradlew clean run --stacktrace
```

---

## Roadmap

### Near-term

```txt
Automatic whitespace cleanup
Better error messages with line/column
Template caching
Custom user-defined filters
Builder/config API
Optional .piped.html extension support
Unit and integration tests
```

### Integration

```txt
Spring Boot MVC integration
Spring ViewResolver
HTMX demo controller
```

### Internal refactor

The current engine is feature-rich but growing large.

Future internal architecture:

```txt
Tokenizer
Parser
AST nodes
Renderer
Expression evaluator
Template loader
Template cache
```

The goal is to keep PTE syntax simple while making the implementation easier to maintain.

---

## Philosophy

PTE is small, readable, and HTML-first.

Core principles:

```txt
Safe output by default
Clear syntax over clever syntax
Readable operators
Explicit block closing
No accidental switch fallthrough
No arbitrary Java method execution in templates
Framework-independent core
HTMX-friendly partial rendering
Reusable layouts and components
```

---

## License

This project does not have a license yet.

Add a license before publishing or accepting outside contributions.