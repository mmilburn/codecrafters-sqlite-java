# About the Project

This is a finished Java implementation for the
["Build Your Own SQLite" Challenge](https://codecrafters.io/challenges/sqlite).
This code implements functionality for all stages (and extentions) of the challenge as of 2025-02-22.

## What can it do?

1. Print the page size and number of tables in a sqlite3 database via the `.dbinfo` command.
2. Print the names of tables in a sqlite3 database via `.tables`.
3. Do a simple `COUNT(*)` operation (`./your_program.sh sample.db "select count(*) from apples"`).
4. Read data from a single column (`./your_program.sh sample.db "SELECT name FROM apples"`).
5. Read data from multiple columns (`./your_program.sh sample.db "SELECT name, color FROM apples"`).
6. Filter data with a `WHERE` clause (only supports `=` operator and only compares against `TEXT` or `VARCHAR` columns
   `./your_program.sh sample.db "SELECT name, color FROM apples WHERE color = 'Yellow'"`).
7. Retrieve data with a full-table scan (
   `./your_program.sh superheroes.db "SELECT id, name FROM superheroes WHERE eye_color = 'Pink Eyes'"`).
8. Retrieve data using an index (
   `./your_program.sh companies.db "SELECT id, name FROM companies WHERE country = 'eritrea'"`).

# Improvements

* Add more restrictive access modifiers and rearrange packages to enable that.
* Handle `COUNT(*)` more gracefully in the `query` package.

# Test Run Video

A short video of the code being run in the codecrafters test environment:

https://github.com/user-attachments/assets/0458b0f4-c5d0-43b1-9ce8-cdf2f0ee7c6d