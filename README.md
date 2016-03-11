CLion-GoogleCppStyleCompletion
==============================

A CLion plugin that provides [Google C++ Style](https://google.github.io/styleguide/cppguide.html) code completion suggestions.

For example, if you are declaring a variable with type "YourCustomizedClass", clion will provide several suggestions like "yourCustomizedClass", "yourCustomized", "your", etc. This plugin adds an additional suggestion "your_customized_class".

Features
========

The following elements are supported by this plugin:

 - Variable names. (YourCustomizedClass -> *your_customized_class*)
 - Member variable names. (YourCustomizedClass -> *your_customized_class_*)
 - Header guards. (base/file.h -> *BASE_FILE_CC_H_*)
 - Namespace names. (base/file.h -> namespace *base*)

Requirements
============

CLion since build 139. Previous version may also work.

Usage
=====

 - Install this plugin
 - No other settings needed. It should work now.

Compile this plugin
===================

 - Please follow [PluginDevelopment](http://www.jetbrains.org/intellij/sdk/docs/) to setup up your IDE
 - Open this project and compile
 - Click "Build" menu and select "Prepare Plugin Module CLion-GoogleCppStyleCompletion For Deployment"
 - Install the generated .jar file in the "Settings -> Plugins" dialog.
 - 
Known issue
===========

 - Suggestions provided by this plugin rank low. They usually appear at the last position. Fix it if you know how.
