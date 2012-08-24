#!/usr/bin/env ruby

require 'rexml/document'

# manifest update
file = File.read("Paco/AndroidManifest.xml")
doc = REXML::Document.new(file)
current_version = doc.elements["manifest"].attributes["android:versionCode"].to_i
doc.elements["manifest"].attributes["android:versionCode"] = "#{current_version + 1}"

major_version,minor_version,dot_version = doc.elements["manifest"].attributes["android:versionName"].split(".").map { |s| s.to_i }
new_dot_version = dot_version + 1
new_version_name = "#{major_version}.#{minor_version}.#{new_dot_version}"
doc.elements["manifest"].attributes["android:versionName"] = new_version_name
File.open("Paco/AndroidManifest.xml", 'w+') { |f|  f.puts doc }

# strings resource file update
file = File.read("Paco/res/values/strings.xml")
xml = REXML::Document.new(file)
version_element = xml.elements["resources"].elements.find {|e| e.attributes["name"] == "version" }
version_element.text = new_version_name
file = File.open("Paco/res/values/strings.xml", 'w+') { |f| f.puts xml }


# server version updating
file = File.read("Paco-Server/war/WEB-INF/appengine-web.xml")
xml = REXML::Document.new(file)
current_server_version =  xml.elements["appengine-web-app"].elements["version"].text.to_i
xml.elements["appengine-web-app"].elements["version"].text = "#{current_server_version + 1}"
file = File.open("Paco-Server/war/WEB-INF/appengine-web.xml", 'w+') { |f| f.puts xml }

# server - Android updatechecker version number
File.open("Paco-Server/war/version", 'w+') { |f| f.puts new_dot_version }
