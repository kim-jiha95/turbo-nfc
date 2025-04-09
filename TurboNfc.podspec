require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "TurboNfc"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "13.0" }
  s.source       = { :git => "https://github.com/kim-jiha95/turbo-nfc.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm}"

  s.dependency "React-Core"
  s.framework = "CoreNFC"
  
 # NFC Capability 설정
  s.pod_target_xcconfig = { 
    'OTHER_LDFLAGS' => '-framework CoreNFC',
    'TARGETED_DEVICE_FAMILY' => '1',
    'IPHONEOS_DEPLOYMENT_TARGET' => '13.0'
  }
  
  # This part installs all required dependencies like Fabric, React-Core, etc.
  install_modules_dependencies(s)
end