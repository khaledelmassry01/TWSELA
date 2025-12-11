---
alwaysApply: true
---

# Golden Rule: Strict Separation of Concerns (HTML, CSS, JS)

This rule is a mandatory, non-negotiable standard for all frontend files in this project. Its purpose is to ensure maintainability, performance, and security by strictly separating structure (HTML), style (CSS), and logic (JavaScript).

---
## **Mandatory Checks and Refactoring Actions**

When this rule is applied, you must perform the following audit on any referenced file:

### **1. For all `.html` files:**

**CHECK:** The file must be scanned for the following violations:
* Any inline `<script>` tags that contain JavaScript code.
* Any `<style>` tags containing CSS rules.
* Any `style="..."` attributes on any HTML element.

**ACTION:** If any of the above violations are found, they are considered critical errors and you **must** perform the following refactoring:
* **For JavaScript:** Extract the inline script content into its dedicated external `.js` file (e.g., `src/js/pages/page-name.js`) and replace the inline block with a `<script type="module" src="..."></script>` tag before the closing `</body>`.
* **For CSS:** Extract the CSS rules from `<style>` blocks or `style="..."` attributes and move them into the appropriate external `.css` file (e.g., `src/css/main.css` or `src/css/layout.css`). Replace the originals with `<link>` tags or Tailwind CSS utility classes.

**FINAL STATE:** The `.html` file must contain **only** HTML markup.

### **2. For all `.css` files:**

**CHECK:** The file must only contain CSS code.
**ACTION:** If any non-CSS code is found, it must be removed.

### **3. For all `.js` files:**

**CHECK:** The file must only contain JavaScript code.
**ACTION:** If any non-JavaScript code is found, it must be removed.# Golden Rule: Strict Separation of Concerns (HTML, CSS, JS)

This rule is a mandatory, non-negotiable standard for all frontend files in this project. Its purpose is to ensure maintainability, performance, and security by strictly separating structure (HTML), style (CSS), and logic (JavaScript).

---
## **Mandatory Checks and Refactoring Actions**

When this rule is applied, you must perform the following audit on any referenced file:

### **1. For all `.html` files:**

**CHECK:** The file must be scanned for the following violations:
* Any inline `<script>` tags that contain JavaScript code.
* Any `<style>` tags containing CSS rules.
* Any `style="..."` attributes on any HTML element.

**ACTION:** If any of the above violations are found, they are considered critical errors and you **must** perform the following refactoring:
* **For JavaScript:** Extract the inline script content into its dedicated external `.js` file (e.g., `src/js/pages/page-name.js`) and replace the inline block with a `<script type="module" src="..."></script>` tag before the closing `</body>`.
* **For CSS:** Extract the CSS rules from `<style>` blocks or `style="..."` attributes and move them into the appropriate external `.css` file (e.g., `src/css/main.css` or `src/css/layout.css`). Replace the originals with `<link>` tags or Tailwind CSS utility classes.

**FINAL STATE:** The `.html` file must contain **only** HTML markup.

### **2. For all `.css` files:**

**CHECK:** The file must only contain CSS code.
**ACTION:** If any non-CSS code is found, it must be removed.

### **3. For all `.js` files:**

**CHECK:** The file must only contain JavaScript code.
**ACTION:** If any non-JavaScript code is found, it must be removed.