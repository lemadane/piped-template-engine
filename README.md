# Piped Template Engine

**Piped Template Engine**, or simply **Piped Template**, is a lightweight Java template engine that uses pipe-based syntax for server-side HTML rendering.

The goal of PTE is to provide a simple, readable, safe-by-default, and HTMX-friendly template engine for Java MVC applications.

```html
<h1>|title|</h1>
<p>Hello, |user.name|</p>
```

## Project Status

This project is currently in early development.

Current focus:

```txt
Feature 0: Project setup
Feature 1: Escaped output with |expr|
Feature 2: HTML and attribute output
Feature 3: Conditional rendering
Feature 4: Loop rendering
Feature 5: Switch rendering
Feature 6: Include and partial rendering
Feature 7: Optional chaining and fallback
```

## Why Piped?

Most Java template engines are powerful, but some can feel verbose for small MVC and HTMX-style applications.

PTE is designed to be:

```txt
Simple
Readable
Server-side
HTML-first
HTMX-friendly
Framework-independent
Safe by default
```

## Basic Syntax

### Escaped Output

```html
<h1>|title|</h1>
<p>|user.name|</p>
```

`|expr|` evaluates an expression and outputs escaped HTML.

Example:

```html
<p>|comment|</p>
```

If `comment` contains:

```html
<strong>Hello</strong>
```

The rendered result becomes:

```html
<p>&lt;strong&gt;Hello&lt;/strong&gt;</p>
```

This makes normal output safe by default.

## Trusted HTML Output

```html
<div>
  |html post.bodyHtml|
</div>
```

`|html expr|` evaluates an expression and outputs it as trusted HTML without escaping.

Use this only for trusted or sanitized HTML.

Example:

```html
<article>
  |html post.bodyHtml|
</article>
```

If `post.bodyHtml` contains:

```html
<p>This is <strong>server-rendered</strong> HTML.</p>
```

The rendered result becomes:

```html
<article>
  <p>This is <strong>server-rendered</strong> HTML.</p>
</article>
```

## Attribute-Safe Output

```html
<img src="|attr user.avatarUrl|" alt="|attr user.name|">
```

`|attr expr|` evaluates an expression and escapes it for safe use inside an HTML attribute.

Example:

```html
<input value="|attr user.name|">
```

If `user.name` contains:

```txt
Juan "Boss" Dela Cruz
```

The rendered result becomes:

```html
<input value="Juan &quot;Boss&quot; Dela Cruz">
```

Use `|attr expr|` for values inside HTML attributes such as:

```html
href="|attr url|"
src="|attr imageUrl|"
alt="|attr imageAlt|"
value="|attr formValue|"
title="|attr tooltip|"
class="|attr className|"
```

## Conditional Rendering

```html
|if user|
  <p>Hello, |user.name|</p>
|else|
  <p>Hello, guest</p>
|/if|
```

PTE uses readable word-based boolean operators instead of symbolic JavaScript-style operators.

Recommended:

```html
|if not user|
  <a href="/login">Login</a>
|/if|
```

```html
|if user.isActive and user.isVerified|
  <p>Verified active user.</p>
|/if|
```

```html
|if user.isAdmin or user.isManager|
  <a href="/dashboard">Dashboard</a>
|/if|
```

## Boolean Operators

PTE supports these word-based boolean operators:

```txt
not
and
or
nand
nor
```

### `not`

```html
|if not user.name|
  <p>Name is missing.</p>
|/if|
```

Meaning:

```txt
if user.name is not present or not truthy
```

### `and`

```html
|if user.isActive and user.isVerified|
  <span>Active verified account</span>
|/if|
```

Meaning:

```txt
true only when both expressions are true
```

### `or`

```html
|if user.isAdmin or user.isManager|
  <a href="/admin">Admin Area</a>
|/if|
```

Meaning:

```txt
true when at least one expression is true
```

### `nand`

```html
|if user.isActive nand user.isVerified|
  <span>Not both active and verified</span>
|/if|
```

Meaning:

```txt
not (user.isActive and user.isVerified)
```

### `nor`

```html
|if user.isAdmin nor user.isManager|
  <span>Regular user</span>
|/if|
```

Meaning:

```txt
not (user.isAdmin or user.isManager)
```

## Comparison Operators

PTE expressions should support these comparison operators:

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
|if user.role == "admin"|
  <a href="/admin">Admin Dashboard</a>
|/if|
```

```html
|if total > 0|
  <p>Total: |total|</p>
|else|
  <p>No balance.</p>
|/if|
```

## Loop Rendering

```html
<ul>
  |each product in products|
    <li>|product.name|</li>
  |else|
    <li>No products found.</li>
  |/each|
</ul>
```

`|each item in items|` loops over a collection.

The optional `|else|` block renders when the collection is empty or missing.

## Each Metadata

Inside an `each` block, PTE should expose **each metadata**.

Official syntax:

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
<table>
  |each user in users|
    <tr class="|if each.even|even|else|odd|/if|">
      <td>|each.index|</td>
      <td>|user.name|</td>
      <td>|user.email|</td>
    </tr>
  |/each|
</table>
```

PTE intentionally avoids `$each` and `$loop`.

Use this:

```html
|each.index|
```

Not this:

```html
|$each.index|
|$loop.index|
```

## Switch Rendering

PTE supports `|switch expr|` for multi-branch rendering.

Basic example:

```html
|switch order.status|
  |case "paid"|
    <span class="badge badge-success">Paid</span>

  |case "pending"|
    <span class="badge badge-warning">Pending</span>

  |case "cancelled"|
    <span class="badge badge-danger">Cancelled</span>

  |default|
    <span class="badge badge-muted">Unknown</span>
|/switch|
```

The expression after `switch` is evaluated once:

```html
|switch expr|
```

Each `case` is compared against the switch value:

```html
|case "paid"|
```

If no case matches, the optional `default` block is rendered:

```html
|default|
```

## Switch Does Not Fall Through by Default

PTE switch cases automatically break by default.

That means this:

```html
|switch order.status|
  |case "paid"|
    Paid

  |case "pending"|
    Pending

  |default|
    Unknown
|/switch|
```

behaves like:

```txt
If status is "paid", render Paid and stop.
If status is "pending", render Pending and stop.
Otherwise, render Unknown.
```

There is no accidental fallthrough.

## Explicit Fallthrough

To continue rendering the next case, use:

```html
|fallthrough|
```

Example:

```html
|switch user.role|
  |case "admin"|
    <a href="/admin">Admin Dashboard</a>
    |fallthrough|

  |case "manager"|
    <a href="/reports">Reports</a>
    |fallthrough|

  |case "user"|
    <a href="/account">My Account</a>

  |default|
    <a href="/login">Login</a>
|/switch|
```

If `user.role` is `"admin"`, this renders:

```html
<a href="/admin">Admin Dashboard</a>
<a href="/reports">Reports</a>
<a href="/account">My Account</a>
```

If `user.role` is `"manager"`, this renders:

```html
<a href="/reports">Reports</a>
<a href="/account">My Account</a>
```

If `user.role` is `"user"`, this renders:

```html
<a href="/account">My Account</a>
```

Switch syntax summary:

```txt
|switch expr|       starts switch block
|case value|        matches against switch expression
|default|           runs when no case matches
|fallthrough|       continues into the next case
|/switch|           closes switch block
```

## Include and Partial Rendering

PTE uses `|include expr|` to render another template or partial.

```html
|include partials/header|

<main>
  <h1>|title|</h1>
</main>

|include partials/footer|
```

`|include expr|` is the official include syntax.

PTE intentionally avoids:

```html
|partial expr|
```

because `include` is clearer and more expressive.

## Include With Custom Context

```html
|each product in products|
  |include partials/product-card with product|
|/each|
```

`partials/product-card.piped.html`:

```html
<div class="product-card">
  <h2>|name|</h2>
  <p>₱|price|</p>
</div>
```

Because the partial is included `with product`, the partial can use:

```html
|name|
|price|
```

instead of:

```html
|product.name|
|product.price|
```

## Optional Chaining and Fallback

PTE should support optional chaining with `?.`.

```html
<p>Hello, |user?.profile?.displayName|</p>
```

If `user` or `profile` is missing, the expression returns nothing instead of throwing.

PTE should also support fallback with `??`.

```html
<p>Hello, |user?.profile?.displayName ?? "Guest"|</p>
```

Example:

```html
<p>|invoice?.customer?.companyName ?? invoice?.customer?.fullName ?? "Walk-in Customer"|</p>
```

This means:

```txt
Use invoice.customer.companyName if present.
Otherwise use invoice.customer.fullName.
Otherwise use "Walk-in Customer".
```

## Design Rules

PTE follows these design decisions:

```txt
|expr|              escaped HTML output
|html expr|         trusted/unescaped HTML output
|attr expr|         attribute-safe output
|if expr|           conditional block
|each item in list| loop block
|switch expr|       switch block
|case value|        switch case
|default|           switch default
|fallthrough|       explicit switch fallthrough
|include expr|      include another template or partial
|/if|               explicit if-block closing
|/each|             explicit each-block closing
|/switch|           explicit switch-block closing
not                 boolean NOT
and                 boolean AND
or                  boolean OR
nand                boolean NAND
nor                 boolean NOR
?.                  optional chaining
??                  fallback value
```

The following syntax is intentionally avoided:

```txt
|! expr|            avoided because not should be used for negation
|< expr|            avoided because < is confusing inside HTML
|partial expr|      avoided in favor of |include expr|
||expr||            avoided because or should be used for boolean OR
|$ expr|            avoided for normal output
|& expr|            avoided because & is confusing with HTML entities
!                   avoided in official syntax; use not
&&                  avoided in official syntax; use and
||                  avoided in official syntax; use or
$each.index         avoided; use each.index
$loop.index         avoided; use each.index
```

## Requirements

```txt
Java 17 or higher
Gradle
JUnit 5
```

The project targets Java 17 as the minimum version so it can be used in many Java applications, while still being compatible with newer Java versions such as Java 25.

## Project Structure

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
    test/
      java/
        com/
          piped/
            template/
              TemplateEngineTest.java
```

## Getting Started

Create the project:

```bash
mkdir piped-template-java
cd piped-template-java
```

Run the tests:

```bash
./gradlew test
```

On Windows PowerShell:

```powershell
.\gradlew.bat test
```

## Current Minimal Usage

At the beginning, the engine starts with a simple render method:

```java
TemplateEngine engine = new TemplateEngine();

String html = engine.render(
    "<h1>Hello Piped</h1>",
    Map.of()
);

System.out.println(html);
```

Output:

```html
<h1>Hello Piped</h1>
```

After Feature 1, this will be supported:

```java
TemplateEngine engine = new TemplateEngine();

String html = engine.render(
    "<h1>|title|</h1>",
    Map.of("title", "Dashboard")
);

System.out.println(html);
```

Expected output:

```html
<h1>Dashboard</h1>
```

## Example Future Usage

```java
TemplateEngine engine = new TemplateEngine();

String html = engine.render(
    """
    <article>
      <h1>|post.title|</h1>
      <p>By |post.author.name|</p>

      <div>
        |html post.bodyHtml|
      </div>
    </article>
    """,
    Map.of(
        "post", Map.of(
            "title", "Building Piped Template Engine",
            "author", Map.of("name", "Lemuel Adane"),
            "bodyHtml", "<p>PTE is a <strong>server-side</strong> template engine.</p>"
        )
    )
);

System.out.println(html);
```

Expected output:

```html
<article>
  <h1>Building Piped Template Engine</h1>
  <p>By Lemuel Adane</p>

  <div>
    <p>PTE is a <strong>server-side</strong> template engine.</p>
  </div>
</article>
```

## Partial Rendering Goal

PTE should support rendering full pages and partial fragments.

Full page rendering:

```java
String html = engine.render("pages/products", model);
```

Partial rendering:

```java
String html = engine.renderPartial("partials/product-card", product);
```

This makes PTE useful for HTMX applications.

PTE renders the HTML fragment on the server. HTMX can then request that fragment and swap it into the existing page.

```txt
PTE  = creates HTML
HTMX = requests and swaps HTML
```

## Example HTMX Use Case

Page template:

```html
<form
  hx-post="/products"
  hx-target="#products"
  hx-swap="beforeend"
>
  <input name="name" required>
  <button>Add Product</button>
</form>

<div id="products">
  |each product in products|
    |include partials/product-card with product|
  |/each|
</div>
```

Partial template:

```html
<div class="product-card">
  <h2>|name|</h2>
  <p>₱|price|</p>
</div>
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

The browser receives only the new product card, not the whole page.

## Development Roadmap

### Feature 0: Project Setup

```txt
Gradle Java library
JUnit 5
Basic TemplateEngine class
Basic smoke test
```

### Feature 1: Escaped Output

```txt
Support |expr|
Read values from Map
Read values from Java records
Read values from JavaBean getters
Escape HTML by default
```

### Feature 2: Output Modes

```txt
Support |html expr|
Support |attr expr|
Add HtmlEscaper
Add AttributeEscaper
```

### Feature 3: Conditionals

```txt
Support |if expr|
Support |else|
Support |/if|
Support word-based boolean operators
Support not, and, or, nand, nor
Support comparison operators
```

### Feature 4: Each Blocks

```txt
Support |each item in items|
Support |else|
Support |/each|
Support each metadata
```

Each metadata:

```html
|each.index|
|each.index0|
|each.first|
|each.last|
|each.even|
|each.odd|
```

### Feature 5: Switch Blocks

```txt
Support |switch expr|
Support |case value|
Support |default|
Support |fallthrough|
Support |/switch|
Automatic break by default
Explicit fallthrough only when |fallthrough| is used
```

### Feature 6: Includes and Partials

```txt
Support |include name|
Support |include name with expr|
Support renderPartial(...)
Detect circular includes
```

### Feature 7: Optional Chaining and Fallback

```txt
Support ?.
Support ??
```

Example:

```html
|user?.profile?.displayName ?? "Guest"|
```

### Feature 8: File-Based Templates

```txt
Load templates from views directory
Support template caching
Support development reload mode
```

### Feature 9: Spring Boot Integration

```txt
Create optional Spring integration
Add ViewResolver support
Add configuration properties
```

## Testing

Run all tests:

```bash
./gradlew test
```

Run clean test:

```bash
./gradlew clean test
```

## Package Naming

Main Java package:

```txt
com.piped.template
```

Suggested future packages:

```txt
com.piped.template.escape
com.piped.template.expression
com.piped.template.parser
com.piped.template.token
com.piped.template.ast
com.piped.template.loader
```

## Philosophy

PTE should stay small and understandable.

Core principles:

```txt
Safe output by default
Clear syntax over clever syntax
Useful errors
Readable operators
Explicit block closing
No accidental switch fallthrough
No arbitrary Java method execution in templates
Framework-independent core
HTMX-friendly partial rendering
```

## License

This project does not have a license yet.

Add a license before publishing or accepting outside contributions.
