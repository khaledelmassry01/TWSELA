// Node script to convert docs/business-model.md to docs/Twsela-Business-Model.docx
// Requires: npm i marked html-to-docx

const fs = require('fs');
const path = require('path');
const { marked } = require('marked');
const htmlToDocx = require('html-to-docx');

(async () => {
  try {
    const mdPath = path.resolve(__dirname, '..', 'docs', 'business-model.md');
    const outPath = path.resolve(__dirname, '..', 'docs', 'Twsela-Business-Model.docx');

    if (!fs.existsSync(mdPath)) {
      console.error(`Source Markdown not found: ${mdPath}`);
      process.exit(1);
    }

    const md = fs.readFileSync(mdPath, 'utf8');

    // Configure marked for headings/tables
    marked.setOptions({
      gfm: true,
      breaks: false,
      headerIds: true
    });

    const html = `<!DOCTYPE html>
<html lang="en" dir="ltr">
<head>
<meta charset="UTF-8" />
<style>
  body { font-family: 'Noto Sans Arabic', 'Segoe UI', Tahoma, Arial, sans-serif; line-height: 1.5; font-size: 11pt; color: #111; }
  h1,h2,h3,h4 { font-weight: 700; }
  h1 { font-size: 20pt; margin: 0 0 12pt 0; }
  h2 { font-size: 16pt; margin: 18pt 0 8pt 0; }
  h3 { font-size: 13pt; margin: 14pt 0 6pt 0; }
  p, li { font-size: 11pt; }
  ul, ol { margin: 0 0 8pt 0; }
  table { border-collapse: collapse; width: 100%; margin: 8pt 0; }
  th, td { border: 1px solid #ccc; padding: 4pt 6pt; }
  code { font-family: Consolas, 'Courier New', monospace; background: #f7f7f7; padding: 1pt 3pt; }
</style>
</head>
<body>
${marked.parse(md)}
</body>
</html>`;

    const fileBuffer = await htmlToDocx(html, null, {
      table: { row: { cantSplit: true } },
      footer: false,
      header: false,
      margins: { top: 720, right: 720, bottom: 720, left: 720 }, // 0.5 inch
      font: 'Noto Sans Arabic'
    });

    fs.writeFileSync(outPath, fileBuffer);
    console.log(`DOCX generated at: ${outPath}`);
  } catch (err) {
    console.error('Failed to generate DOCX:', err);
    process.exit(1);
  }
})();
