urop demo
========

How to run
---

- Pull this repo, import into SDK, and run on an Android device
- Alternatively, install this [apk](https://dl.dropboxusercontent.com/u/42083231/UROPDemo.apk) file and run
- To record a video:
  - choose record video on main page
  - click on REC button in top right corner to start recording
  - click again to stop recording
- To watch video with synchronized sensor data:
  - choose play video on main page
  - click on PLAY button to start watching the video
  
Notes
---
- This app was tested and is working on Galaxy SIII
- Sensor data being recorded is the device's orientation in the three axes
- Threshold value during playback is y>|20|
  - This corresponds to instances in which the device (held in landscape orientaion) is rotated more than 20 degrees from the horizontal
- A lot of camera code is borrowed from [here](http://android-er.blogspot.com/2011/10/simple-exercise-of-video-capture-using.html)
- Video/sensor data is being saved in root sdcard directory
  - Video has max size 50M or max duration 60s
  - Sensor data stored as tsv in txt file
