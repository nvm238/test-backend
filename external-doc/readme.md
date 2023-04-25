# Building a pdf

Requirements:

- `bundle` (ruby's package manager)
- Google Chrome 

To build a pdf:

- Make sure you've updated the section 'Version history' in the document: update the current version, and add changes to the changelog section
- Run `./to-pdf.sh`
- Rename pdf to include version number, like `medicinfo-api-documentation-v0.8.pdf`
- Upload doc to Drive: https://drive.google.com/drive/folders/1BLf1_dlqBXIh_wKMwj_WEs0PVE5Ilznh
- Send to MedicInfo

# Writing documentation

All documentation is in `index.markdown`. HTML layout is in`_layouts`. CSS is in `css` (but mostly we just borrow
the rules from Jekyll's default template).

To start writing:

- Run `bundle exec jekyll serve` and open http://localhost:4000/
- Changes are reflected in the html each time you save a file

# Rationale

Using Jekyll is a bit overkill for just converting markdown to pdf, ie. `pandoc` can do it in one step.
But by using Jekyll, we can make use of it's template which gives us a head start to make the output pretty.

# Other approaches

- Tried `wkhtmltopdf` but it gave horribly results. Fonts didn't render, lines were overflowing over pages etc.
- We could look into Weasyprint, but out-of-the-box results don't look too good (svg can't be rendered, too long lines etc)
- Maybe try `electron-pdf`
