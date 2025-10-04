For your next release
Edge-to-edge may not display for all users
From Android 15, apps targeting SDK 35 will display edge-to-edge by default. Apps targeting SDK 35 should handle insets to make sure that their app displays correctly on Android 15 and later. Investigate this issue and allow time to test edge-to-edge and make the required updates. Alternatively, call enableEdgeToEdge() for Kotlin or EdgeToEdge.enable() for Java for backward compatibility.

User experience
Release name: 101010400 (1.1.1)
Your app uses deprecated APIs or parameters for edge-to-edge
One or more of the APIs you use or parameters that you set for edge-to-edge and window display have been deprecated in Android 15. To fix this, migrate away from these APIs or parameters.

User experience
Release name: 101010400 (1.1.1)
Implement picture-in-picture to improve your app quality and user experience
Picture-in-picture can achieve high user engagement because it lets users watch a video in a small window pinned to a corner of the screen, while they navigate between apps or browse content on the main screen.

User experience
Release name: 101010400 (1.1.1)
Peers MAU
71.20%
Remove resizability and orientation restrictions in your app to support large screen devices
From Android 16, Android will ignore resizability and orientation restrictions for large screen devices, such as foldables and tablets. This may lead to layout and usability issues for your users.

