# Regatta Timer
This App is a Regatta Timer for Android Smartwatches.

## Overview
The Regatta Timer acts like an Optimum Time Series 3 watch which is used on sailing regattas. It has all the timer ans stop watch functionality implemented as the regular watch.

<img src="https://cloud.githubusercontent.com/assets/1078036/7267710/614d33bc-e8bf-11e4-8864-08a16917460c.png" alt="Android Wear Round" width="50%" height="50%">
<img src="https://cloud.githubusercontent.com/assets/1078036/7267656/dcb23aee-e8be-11e4-8ad6-0eb5568039d3.png" alt="Android Wear Rect" width="50%" height="50%">

## Usage

#### Actions
Prog: Adds the configured amount of time to the count down.<br>
Clear: Resets the the count down and stop watch.<br>
Start/Stop: If a count down time is set it starts/stops the couting down. Otherwise a stop watch (count up) ist started/stopped.<br>
Sync: Synchronizes the timer in count down mode to the next whole minute.

#### Changing Settings (long touch):
Prog: Changing amount of time to be added. Available modes: 5,4,1,0 / 3M / 2M / 1M<br>
Clear: Changing interval mode between repeating and up/down.

#### Acustic Signals:
In count down mode the timer gives different beep signals indicating how much time is available to zero.

Every minute: Beep<br>
In last minute: <br>
- every 10 sec: Beep<br>
- last 15 secs: two short beeps every second<br>
- zero time: long beep

## Contributing
For making contributions please send me pull requests, but also bugs and enhancement requests are welcome. Although no guarantees on when I can review them.

## License

* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
