# update-projects

a script to make the flow of "increment multiple projects' version numbers at the same time and update their dependencies on each other" a bit faster

## Usage

lein run project1 project2 project3 # assumes your username is gregor and you're on a Mac, obviously. will update project3's dependencies on project1 and project2 but not project1's on project2 or project3

### Bugs

          |
          |
         _|
    ///\(o_o)/\\\
    |||  ` '  |||

## License

Copyright © 2013 Gregor Stocks

Distributed under the Eclipse Public License, the same as Clojure.
