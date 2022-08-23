# pnd-service

Pandora is an authentication service for Instagram.

## Installation

Include manifst internet permission in your proyect (AndroidManifest.xml)

```bash
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

Include jitpack in your proyect (build.gradle)

```bash
repositories {
        maven { url 'https://jitpack.io' }
}
```

Add implementation
```bash
dependencies {
   implementation 'com.github.boxpnd:pnd-service:1.0.0'
}
```

## Basic Usage - Init 

```java
PandoraService pandora = new PandoraService();
pandora.setAuthListener(new PandoraService.PandoraListener() {

@Override
public void onSuccced(String user_id) {
    Log.d("TAG","Login succed"); // Reward the user for authenticating
}

@Override
public void onFailed(String msg) {
    Log.d("TAG","Login failed"); // Authentication failed
}

@Override
public void onCancelled(String msg) {
    Log.d("TAG","Login cancelled"); // The user canceled the authentication
}
}, YourActivity.this);
```
Then we call the auth() function to create the login screen

```java
pandora.auth() // You can place it inside a login button
```
## Questions
If you have doubts or questions about how to use it, do not hesitate to send me an issue

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)
