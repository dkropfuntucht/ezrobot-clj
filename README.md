# ezrobot-clj

A Clojure library for speaking EZ-B Protocol Messages at an EZ-Robot.

## Installation

Add the following dependency to your project.clj file:

```[ezrobot-clj "0.1.0"]```

(once it's on Clojars)

## Usage

This simple code will connect to an EZ-Robot and move the servo on 0:

```
(ns hello-robot
  (:require [ezrobot-clj.core :as ez]))

 (def robot (ez/connect-async "your robot ip" 23))

(println (ez/handshake robot))
(println (ez/get-voltage robot))

(ez/move-servo! robot 0 20 20)
```

If you hear grinding noises or smell smoke, this may be helpful:

```(ez/release-servos! robot)```

## Roadmap
1. Complete state-map and watch based movement system
2. Refactor by moving a few of the functions to separate namespaces
3. Post to Clojars
4. Bring up the UART commands
5. More refinement to map-based movement commands
6. Connect robot-state to nn and see if it can learn to balance
7. Teach Jd some Aikido
8. Build a bridge to ROS via rosclj

## License

Copyright © 2017 Damon Kropf-Untucht

Distributed under the Eclipse Public License, the same as Clojure.
