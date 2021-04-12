# FFmpegVideoCompressor
Compress videos without reducing quality using FFmpeg

APP SCREENSHOTS:

![Alt text](https://github.com/cvivek07/FFmpegVideoCompressor/blob/master/screenshots/device-2021-04-12-150807.png?raw=true "Optional Title")

![Alt text](https://github.com/cvivek07/FFmpegVideoCompressor/blob/master/screenshots/device-2021-04-12-165839.png?raw=true "Optional Title")


How to

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

gradle

Add it in your root build.gradle at the end of repositories:


	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
  
  Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.cvivek07:FFmpegVideoCompressor:0.1.0'
	}
	
Results:

281 MB to 46 MB (62 seconds)
19 MB to 2 MB (11 seconds)
65 MB to 18 MB (30 seconds)

