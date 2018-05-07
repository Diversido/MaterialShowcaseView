# MaterialShowcaseView
A Material Design themed ShowcaseView for Android

This library has been forked from [deano2390][6] which in turn is heavily inspired by the original [ShowcaseView library][1].

I found the [former library][6] good, but I wanted to streamline the library and force users to follow at
least some of the Material design guidelines — namely [onboarding][8] and [feature discovery][7]. Therefor
it is only possible to create fullscreen and target showcases where the target always should be an icon.

![Animation][9] ![Fullscreen][10] ![Sequence][11]

# Gradle
--------

[![jitpack][4]][5]

Add the jitpack repo to your your project's build.gradle at the end of repositories [Why?](#why-jitpack)

/build.gradle
```groovy
allprojects {
	repositories {
		jcenter()
		maven { url "https://jitpack.io" }
	}
}
```

Then add the dependency to your module's build.gradle:

/app/build.gradle
```groovy
compile 'com.github.BlushineIO:MaterialShowcaseView:2.1.0'
```

NOTE: Some people have mentioned that they needed to add the @aar suffix to get it to resolve from JitPack:
```groovy
compile 'com.github.BlushineIO:MaterialShowcaseView:2.1.0@aar'
```

# How to use
--------
Below are some simple examples. You can check out the samples for advanced examples and the API for more settings.
Only one showcase can ever be viewed at the same time, the rest of the showcases are queued.

## Target
--------
To create a circle around a target you only have to supply a target

```java
	// Target example
	new MaterialShowcaseView.Builder(this)
			.setTarget(mFAB) // <-- Make this a target showcase
			.setTitleText("Optional title")
			.setContentText("Optional content text")
			.setDismissText("got it") // Optional. When used can only dismiss the showcase by clicking on the dismiss button and target isn't pressable.
			.setSingleUse(SHOWCASE_ID) // Provide a unique ID to ensure it is only shown once
			.setDelay(500) // Optional. But starting animations immediately in onCreate can make the choppy
			.show();

	// You can also use a config to set default values to use for multiple showcases
	ShowcaseConfig config = new ShowcaseConfig();
	config.setDelay(500);
	new MaterialShowcaseView.Builder(this)
			.setConfig(config)
			.setTarget(mFAB)
			// ...
```
Note: The inner circle for the target is always 88dp as this is the metrics provided by Google. Target button should not be larger than 55dp.

## Fullscreen
-------------
Fullscreen showcases are created by NOT supplying a target

```java
	// Fullscreen
	new MaterialShowcaseView.Builder(this)
			.setTitleText("Optional title")
			.setContentText("Optional content text")
			.setDismissText("got it") // Optional. When used can only dismiss the showcase by clicking on the dismiss button and target isn't pressable.
			.setSingleUse(SHOWCASE_ID) // Provide a unique ID to ensure it is only shown once
			.setDelay(500) // Optional. But starting animations immediately in onCreate can make the choppy
			.show();

	// You can also use a config to set default values to use for multiple showcases
	ShowcaseConfig config = new ShowcaseConfig();
	config.setDelay(500);
	new MaterialShowcaseView.Builder(this)
			.setConfig(config)
			// ...
```

## Sequence
-----------
There are two possible ways to create a sequence. As showcases there can only be one showcase visible at the same time showcases are
automatically queued when calling show().
```java
	// You can use a config to easy set common settings for each showcase
	ShowcaseConfig config = new ShowcaseConfig(this);
	config.setDelay(0);

	MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
	sequence.setConfig(config);

	// 1
	sequence.addSequenceItem(
			new MaterialShowcaseView.Builder(this)
					.setTarget(mTopButton)
					.setTitleText("Press button")
					.setContentText("You can click on the target if you don't call setTargetTouchable(false) or set a hide text")
					.build()
	);


	// We update the config so that there is half second delay between each showcase view
	config.setDelay(500);

	// 2
	sequence.addSequenceItem(mBottomRightButton, "Click outside to hide", "Click outside the area hide", null);

	// 3
	sequence.addSequenceItem(
			new MaterialShowcaseView.Builder(this)
					.setDismissText("got it")
					.setTitleText("Fullscreen showcase")
					.setContentText("You can use both fullsceen and target showcases in your sequence :)")
					.build()
	);

	sequence.show();

	// 4 - Not part of the sequence but queued anyway
	new MaterialShowcaseView.Builder(this)
			.setTitleText("Automatically queued")
			.setContentText("Calling show() on while a sequence or showcase is active will queue the showcase")
			.show();
```

# Why Jitpack
------------
Publishing libraries to Maven is a chore that takes time and effort. Jitpack.io allows me to release without ever leaving GitHub so I can release easily and more often.

# License
-------

    Copyright 2016-2018 Spiddekauga Games AB
    Copyright 2015 Dean Wild

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



[1]: https://github.com/amlcurran/ShowcaseView
[4]: https://img.shields.io/github/release/spiddekauga/MaterialShowcaseView.svg?label=JitPack
[5]: https://jitpack.io/#BlushineIO/MaterialShowcaseView
[6]: https://github.com/deano2390/MaterialShowcaseView
[7]: https://material.google.com/growth-communications/feature-discovery.html
[8]: https://material.google.com/growth-communications/onboarding.html
[9]: http://imgur.com/l5mwSOl.gif
[10]: http://imgur.com/CnUDfSH.gif
[11]: http://imgur.com/LBKCob3.gif
