

Solves White jigsaw puzzles, inspired by the video by Stuff Made Here:
https://www.youtube.com/watch?v=Gu_1S77XkiM

He didn't solve this part in the video, I thought I will take a crack at it, 
Formulation: https://mathb.in/77182


Long story short simply throwing  entire problem  at SAT solver is not very efficient, probably 
solving edges first and moving in in separate steps and hints with a solver might be
more efficient. But for size 11x11 it was solving within minutes, larger ones like 32x32 runs
out of JVM memory (16GB).

It also possible to use this to generate a DIMACS cnf file, I implemented a variant of 
[[Tseitin Transform]https://en.wikipedia.org/wiki/Tseytin_transformation] to make this, and performance
using MiniSAT is worse than using OR Tools, but scales better. 

* TODO: A better formulation that doesn't take GB sized cnf files. 
* TODO: Use better SAT solvers like Glucose.
* TODO: Submit the problems to SAT competition 2023.

