<manifest xmlns:android="http://schemas... >
 <application android:name=".DriveTestApp" ...>
   <activity android:name=".TestActivity"
     android:label="@string/title_activity_drive_test" ...>
    <activity android:name=".MainActivity">
     <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER"/>
     </intent-filter>
   </activity>
    ...
 </application>
</manifest>