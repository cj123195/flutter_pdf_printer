package com.example.flutter_printer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.print.PrintManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterPrinterPlugin */
public class FlutterPrinterPlugin implements FlutterPlugin, MethodCallHandler {
  private static final String CHANNEL_NAME = "flutter_printer";
  private static final String GET_PLATFORM_VERSION = "getPlatformVersion";
  private static final String PRINT_PDF = "printPdf";
  private final MethodChannel channel;
  private final Activity activity;
  private final Context context;
  private final BinaryMessenger messenger;
  private Result pendingResult;
  private MethodCall methodCall;
  private FlutterPluginBinding binding;

  private FlutterPrinterPlugin(Activity activity, Context context,
                               MethodChannel channel, BinaryMessenger messenger) {
    this.channel = channel;
    this.activity = activity;
    this.context = context;
    this.messenger = messenger;

  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    binding = flutterPluginBinding;
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
    FlutterPrinterPlugin instance = new FlutterPrinterPlugin(registrar.activity(),
            registrar.context(), channel, registrar.messenger());
    registrar.addActivityResultListener((PluginRegistry.ActivityResultListener) instance);
    channel.setMethodCallHandler(instance);
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if(!setPendingMethodCallAndResult(call, result)) {
      finishWithAlreadyActiveError(result);
      return;
    }
    if (GET_PLATFORM_VERSION.equals(call.method)) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(PRINT_PDF.equals(call.method)) {
      printPdf();
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  private void printPdf() {
    String path = methodCall.argument("path");
    String name = methodCall.argument("name");
    Log.d("print", "configureFlutterEngine: " + path);
    PrintManager printManager = (PrintManager)
            context.getSystemService(Context.PRINT_SERVICE);
    PdfDocumentAdapter pdfDocumentAdapter = new PdfDocumentAdapter(path, name);
    printManager.print("测试", pdfDocumentAdapter, null);
    if(pdfDocumentAdapter.getSuccess()) {
      pendingResult.success("success");
    } else {
      finishWithError();
    }
  }

  private boolean setPendingMethodCallAndResult(
          MethodCall methodCall, MethodChannel.Result result) {
    if (pendingResult != null) {
      return false;
    }

    this.methodCall = methodCall;
    pendingResult = result;
    return true;
  }

  private void finishWithAlreadyActiveError(MethodChannel.Result result) {
    if (result != null)
      result.error("already_active", "打印正在进行中", null);
  }

  private void finishWithError() {
    if (pendingResult != null)
      pendingResult.error("failed", "打印失败", null);
    clearMethodCallAndResult();
  }

  private void clearMethodCallAndResult() {
    methodCall = null;
    pendingResult = null;
  }
}
