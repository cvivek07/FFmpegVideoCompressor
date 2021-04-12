# FFmpegVideoCompressor
Compress videos without reducing quality using FFmpeg


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
	        implementation 'com.github.cvivek07:FFmpegVideoCompressor:Tag'
	}
