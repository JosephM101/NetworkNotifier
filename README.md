# Network Notifier
A simple Android app and service that lets you know when your phone automatically switches to mobile data.

<img width="350" alt="Screenshot_1776873587" src="https://github.com/user-attachments/assets/4be2684d-7efa-4349-a6d2-0d131e913e6c" />

## Rationale
I wrote this app out of necessity. The Wi-Fi connection can be spotty in our house, and I noticed early on that Android will automatically switch to using mobile data when the Wi-Fi signal/stability is even slightly degraded. This is _very_ annoying behavior since even when the connection is good enough to do most things, it will switch connections anyway, and I will not notice that it switched. Perhaps my phone is in my pocket or I'm in a fullscreen activity where the top bar is hidden (or I just don't notice the icon change). I'll notice when a substantial amount of data has been used.
What's even more annoying is that even when I move closer to the router or to a better spot in the house, my phone will remain on mobile data for some time until its periodic check of network signal strengths, at which point it will switch back to Wi-Fi when it realizes that it is strong enough.

Normally I turn off cellular data when I'm home since it is unnecessary (and I have a limited — albeit cheap mobile phone plan that I don't want to unknowingly use up). However, sometimes I forget to turn off the connection, and I don't realize until it's too late.

## Premise
This app is simple. You install it, grant the required permissions, and it will run continuously in the background. 
When your device automatically switches to a mobile data connection, a notification will be sent alerting you to the change. It may seem a bit like a gimmick, but trust me when I say it's saved my mobile plan these past couple of months. **That's not hyperbole.**
