#import "FlutterPrinterPlugin.h"
#if __has_include(<flutter_printer/flutter_printer-Swift.h>)
#import <flutter_printer/flutter_printer-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_printer-Swift.h"
#endif

@implementation FlutterPrinterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterPrinterPlugin registerWithRegistrar:registrar];
}
@end
