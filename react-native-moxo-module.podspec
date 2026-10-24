require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-moxo-module"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "15.6" }
  s.source       = { :git => "https://github.com/Moxtra/react-native-moxo-module.git", :tag => "#{s.version}" }
  s.source_files = "ios/**/*.{h,m,mm,swift}"
  s.private_header_files = "ios/**/*.h"
  s.swift_version = "5.9"

  # MEPSDK is embedded directly - downloaded via npm postinstall
  s.vendored_frameworks = "ios/Frameworks/MEPSDK.xcframework"

  install_modules_dependencies(s)
end
