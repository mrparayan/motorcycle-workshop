package com.workshop.billing;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.result.ActivityResult;
import androidx.documentfile.provider.DocumentFile;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@CapacitorPlugin(name = "FolderBackup")
public class FolderBackupPlugin extends Plugin {
  @PluginMethod public void pickFolder(PluginCall call) {
    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    startActivityForResult(call, i, "picked");
  }
  @ActivityCallback private void picked(PluginCall call, ActivityResult r) {
    if (call == null) return;
    if (r.getResultCode() != Activity.RESULT_OK || r.getData() == null) { call.reject("cancelled"); return; }
    Uri u = r.getData().getData();
    getContext().getContentResolver().takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    JSObject o = new JSObject(); o.put("uri", u.toString()); call.resolve(o);
  }
  @PluginMethod public void write(PluginCall call) {
    try {
      DocumentFile dir = DocumentFile.fromTreeUri(getContext(), Uri.parse(call.getString("uri")));
      String name = call.getString("name");
      DocumentFile f = dir.findFile(name);
      if (f == null) f = dir.createFile(name.endsWith(".csv") ? "text/csv" : "application/json", name);
      OutputStream os = getContext().getContentResolver().openOutputStream(f.getUri(), "wt");
      os.write(call.getString("data").getBytes(StandardCharsets.UTF_8)); os.close();
      call.resolve();
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
  @PluginMethod public void read(PluginCall call) {
    try {
      DocumentFile dir = DocumentFile.fromTreeUri(getContext(), Uri.parse(call.getString("uri")));
      DocumentFile f = dir.findFile(call.getString("name"));
      JSObject o = new JSObject();
      if (f == null) { o.put("data", ""); call.resolve(o); return; }
      InputStream is = getContext().getContentResolver().openInputStream(f.getUri());
      ByteArrayOutputStream b = new ByteArrayOutputStream(); byte[] buf = new byte[8192]; int n;
      while ((n = is.read(buf)) > 0) b.write(buf, 0, n);
      is.close(); o.put("data", b.toString("UTF-8")); call.resolve(o);
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); }
  }
  @PluginMethod public void printHtml(PluginCall call) {
    final String html = call.getString("html");
    getActivity().runOnUiThread(() -> {
      WebView w = new WebView(getContext());
      w.setWebViewClient(new WebViewClient() {
        @Override public void onPageFinished(WebView v, String u) {
          PrintManager pm = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);
          pm.print("Bill", v.createPrintDocumentAdapter("Bill"), new PrintAttributes.Builder().build());
        }
      });
      w.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
      call.resolve();
    });
  }
}
