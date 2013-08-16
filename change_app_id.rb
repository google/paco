#!/usr/bin/env ruby

require 'rexml/document'

def rename(app_id = "quantifiedself")
  server_name = app_id + ".appspot.com"
  # strings resource file update
  file = File.read("Paco/res/values/strings.xml")
  xml = REXML::Document.new(file)
  server = xml.elements["resources"].elements.find {|e| e.attributes["name"] == "server" }
  server.text = server_name

  about_web_url = xml.elements["resources"].elements.find {|e| e.attributes["name"] == "about_weburl" }
  about_web_url.text = server_name + "/Main.html"
  file = File.open("Paco/res/values/strings.xml", 'w+') { |f| f.puts xml }

  # server appid update
  file = File.read("Paco-Server/war/WEB-INF/appengine-web.xml")
  xml = REXML::Document.new(file)
  current_server_app_id =  xml.elements["appengine-web-app"].elements["application"]
  xml.elements["appengine-web-app"].elements["application"].text = app_id
  file = File.open("Paco-Server/war/WEB-INF/appengine-web.xml", 'w+') { |f| f.  puts xml }
end

if __FILE__ == $0
  if ARGV.length == 0
    puts "need an appid name"
  else
    rename(ARGV[0])
  end
end
