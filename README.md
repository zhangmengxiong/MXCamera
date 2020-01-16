# MXCamera
一个简单的android 拍摄/录像开源工具包，开箱可用
最新版本：[![](https://jitpack.io/v/zhangmengxiong/MXCamera.svg)](https://jitpack.io/#zhangmengxiong/MXCamera)

使用方法：
第一步：根目录添加gitpack的maven仓库
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
第二步：添加implementation
```
	dependencies {
	        implementation 'com.github.zhangmengxiong:MXCamera:xxx'
	}
```


第三步：添加Activity声明/权限声明
```
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />

        <activity
            android:name="com.mx.camera.RecorderActivity"
            android:launchMode="singleTop"
            android:screenOrientation="portrait" />

```
第四步：调用拍摄
```
        startActivityForResult(
            Intent(this, RecorderActivity::class.java)
                .putExtra(RecorderActivity.CONFIG, CameraConfig.createSimplePicConfig(this)), 0x22
        )
        
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == 0x22) {
            val file = (data?.getSerializableExtra(RecorderActivity.RESULT_KEY) as File?) ?: return
            println("${file.absolutePath} ${file.length() / 1024f} Kb")
        }
    }
```
