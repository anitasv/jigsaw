

Solves White jigsaw puzzles, inspired by the video by Stuff Made Here:
https://www.youtube.com/watch?v=Gu_1S77XkiM

He didn't solve this part in the video, I thought I will take a crack at it, 
Formulation: https://mathb.in/77183


Long story short simply throwing  entire problem  at SAT solver is not very efficient, probably 
solving edges first and moving in in separate steps and hints with a solver might be
more efficient. But for size 11x11 it was solving within minutes, larger ones like 32x32 runs
out of JVM memory (16GB).

It also possible to use this to generate a DIMACS cnf file, I implemented a variant of 
[Tseitin Transform](https://en.wikipedia.org/wiki/Tseytin_transformation) to make this, and performance
using MiniSAT is worse than using OR Tools, but scales better. 

```shell
brew install maven 
mvn package
java -jar target/jigsaw-1.0-SNAPSHOT.jar --random --M=5 --N=5
```

Sample Output:
```
Using random jigsaw puzzle
Source diagram:
   -      -      -      -      -
|     <<     <<     >>     >>     |
   ^      V      ^      ^      V
   ^      V      ^      ^      V
|     >>     <<     >>     >>     |
   V      ^      V      ^      V
   V      ^      V      ^      V
|     >>     <<     >>     <<     |
   V      V      ^      ^      ^
   V      V      ^      ^      ^
|     <<     >>     <<     <<     |
   ^      V      ^      V      ^
   ^      V      ^      V      ^
|     >>     <<     <<     >>     |
   -      -      -      -      -

Reconstituted Diagram:
   -      -      -      -      -
|     >>     >>     <<     <<     |
   V      ^      ^      V      ^
   V      ^      ^      V      ^
|     >>     >>     <<     <<     |
   ^      V      V      ^      ^
   ^      V      V      ^      ^
|     >>     <<     <<     >>     |
   ^      ^      ^      V      V
   ^      ^      ^      V      V
|     >>     <<     <<     >>     |
   V      V      ^      V      V
   V      V      ^      V      V
|     <<     >>     >>     <<     |
   -      -      -      -      -

1. (2,3,0) -> (1,1,1)
2. (0,1,3) -> (0,2,1)
3. (0,3,2) -> (1,0,3)
4. (1,0,2) -> (1,4,0)
5. (0,2,1) -> (3,4,2)
6. (3,4,2) -> (4,2,1)
7. (2,4,0) -> (0,3,1)
8. (3,2,0) -> (2,1,1)
9. (3,0,0) -> (4,3,1)
10. (0,4,2) -> (0,0,3)
11. (4,2,2) -> (0,1,0)
12. (0,0,2) -> (4,0,3)
13. (2,2,1) -> (3,1,0)
14. (3,3,0) -> (2,3,3)
15. (1,4,2) -> (3,0,0)
16. (2,0,2) -> (4,1,3)
17. (2,1,0) -> (1,3,0)
18. (1,3,3) -> (1,2,0)
19. (3,1,1) -> (3,2,0)
20. (1,1,3) -> (2,2,0)
21. (4,3,2) -> (2,4,3)
22. (1,2,3) -> (3,3,0)
23. (4,0,1) -> (4,4,0)
24. (4,4,3) -> (0,4,2)
25. (4,1,1) -> (2,0,2)
```

* TODO: Take custom user pieces instead of random.
* TODO: A better formulation that doesn't take GB sized cnf files. 
* TODO: Use better SAT solvers like Glucose.
* TODO: Submit the problems to SAT competition 2023.

