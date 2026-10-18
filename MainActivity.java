package com.workshop.billing;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
  @Override public void onCreate(Bundle s) { registerPlugin(FolderBackupPlugin.class); super.onCreate(s); }
}
