# Five words clique

It's a try at solving the 5 word clique more efficiently than the proposed solution.

See the video that explains the problem: https://www.youtube.com/watch?v=_-AfhLQfb6w

To run you need java installed
```
linux & mac
./gradlew run

windows
./gradlew.bat run
```

The solution is fast **< 2 seconds** because it takes a different approach.

Instead of testing all possibilities, we can discard rapidly the solutions that won't work as soon as possible.  
We use the fact that we know we need 25 of the 26 letters in each valid solution.  
Because of that we can try to fill all letters one by one, and once we have more than one letter missing, we discard the solution.  
It still uses the graph theory, but because we are able to remove most of the options asap we have much less combinations to test.

You can find more technical info in the [Main.kt](./src/main/kotlin/Main.kt).

It also makes use of a bitset representation of the words, but that doesn't make much of a difference.