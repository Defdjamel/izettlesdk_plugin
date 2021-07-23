#import "IzettlesdkPlugin.h"
#if __has_include(<izettlesdk_plugin/izettlesdk_plugin-Swift.h>)
#import <izettlesdk_plugin/izettlesdk_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "izettlesdk_plugin-Swift.h"
#endif

@implementation IzettlesdkPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftIzettlesdkPlugin registerWithRegistrar:registrar];
}
@end
