/**
 *	Vesternet VES-ZW-DIM-001 2-Wire Capable Dimmer
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZW-DIM-001 2-Wire Capable Dimmer", namespace: "Vesternet", author: "Vesternet") {
		capability "Switch"
		capability "Actuator"
		capability "PowerMeter"
		capability "EnergyMeter"
		capability "VoltageMeasurement"	
		capability "CurrentMeter"	
		capability "SwitchLevel"
		capability "ChangeLevel"
		capability "Refresh"
		capability "Configuration"

		attribute "deviceNotification", "string"

		fingerprint mfr: "0330", prod: "0200", deviceId: "D00C", inClusters:"0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-DIM-001 2-Wire Capable Dimmer"
	}
	preferences {
		input name: "powerFailState", type: "enum", title: "Load State After Power Failure", options: [0: "off", 1: "on", 2: "previous state"], defaultValue: 2
		input name: "basicReport", type: "enum", title: "Send Basic Report", options: [0: "disabled", 1: "enabled"], defaultValue: 1
		input name: "defaultFadeTime", type: "number", title: "Fade Time (seconds 0 - 127)", range: "0..127", defaultValue: 1
		input name: "minimumBrightnessValue", type: "number", title: "Minimum Brightness Value (0 - 50)", range: "0..50", defaultValue: 15
		input name: "maximumBrightnessValue", type: "number", title: "Maximum Brightness Value (0 - 100)", range: "0..100", defaultValue: 100
		input name: "mosfetDrivingType", type: "enum", title: "Mosfet Driving Type", options: [0: "trailing edge", 1: "leading edge"], defaultValue: 0
		input name: "switchType", type: "enum", title: "Switch Type Attached", options: [0: "momentary", 1: "toggle"], defaultValue: 0		
		input name: "switchInclusion", type: "enum", title: "Allow Switch Input To Include / Exclude", options: [0: "disabled", 1: "enabled"], defaultValue: 1
		input name: "overCurrentProtection", type: "enum", title: "Overcurrent Protection (2.1A for 20 seconds)", options: [0: "disabled", 1: "enabled"], defaultValue: 1
		input name: "powerReportChangeWatts", type: "number", title: "Power Change Watts (1W - 400W, 0 = disabled)", range: "0..400", defaultValue: 10
		input name: "powerReportChangePercent", type: "number", title: "Power Change Percent (1% - 100%, 0 = disabled)", range: "0..100", defaultValue: 20
		input name: "powerReportTime", type: "enum", title: "Power Time (s)", options: [0: "disabled",10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s",1800:"1800s",3600:"3600s",7200:"7200s"], defaultValue: 600         
		input name: "voltageReportTime", type: "enum", title: "Voltage Time (s)", options: [0: "disabled",10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s",1800:"1800s",3600:"3600s",7200:"7200s"], defaultValue: 3600         
		input name: "currentReportTime", type: "enum", title: "Current Time (s)", options: [0: "disabled",10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s",1800:"1800s",3600:"3600s",7200:"7200s"], defaultValue: 3600         
		input name: "energyReportTime", type: "enum", title: "Energy Time (s)", options: [0: "disabled",10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s",1800:"1800s",3600:"3600s",7200:"7200s"], defaultValue: 1800
		input name: "dimmingCurve", type: "enum", title: "Dimming Curve", options: [0: "linear", 1: "logarithmic"], defaultValue: 0
		input name: "startupBrightness", type: "number", title: "Startup Brightness (0 - 99)", range: "0..99", defaultValue: 0
		input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
		input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
	}
}

def getCommandClassVersions() {
	[ 0x20: 1, 0x26: 3, 0x32: 3, 0x70: 1, 0x71: 8 ]//multilevel 0x26 v4 on device
}

def installed() {
	device.updateSetting("logEnable", [value: "true", type: "bool"])
	logDebug("installed called")
	device.updateSetting("powerFailState", [value: "2", type: "enum"])
	device.updateSetting("basicReport", [value: "1", type: "enum"])
	device.updateSetting("defaultFadeTime", [value: 1, type: "number"])
	device.updateSetting("minimumBrightnessValue", [value: 15, type: "number"])
	device.updateSetting("maximumBrightnessValue", [value: 100, type: "number"])
	device.updateSetting("mosfetDrivingType", [value: "0", type: "enum"])
    device.updateSetting("switchType", [value: "0", type: "enum"])
	device.updateSetting("switchInclusion", [value: "1", type: "enum"])
	device.updateSetting("overCurrentProtection", [value: "1", type: "enum"])
	device.updateSetting("powerReportChangeWatts", [value: 10, type: "number"])
	device.updateSetting("powerReportChangePercent", [value: 20, type: "number"])
	device.updateSetting("powerReportTime", [value: "600", type: "enum"])
	device.updateSetting("voltageReportTime", [value: "3600", type: "enum"])
	device.updateSetting("currentReportTime", [value: "3600", type: "enum"])
	device.updateSetting("energyReportTime", [value: "1800", type: "enum"])
	device.updateSetting("dimmingCurve", [value: "0", type: "enum"])
	device.updateSetting("startupBrightness", [value: 0, type: "number"])
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")
	log.warn("debug logging is: ${logEnable == true}")
	log.warn("power fail state is: ${powerFailState == "0" ? "off" : powerFailState == "1" ? "on" : "previousstate"}")	
	log.warn("basic report is: ${basicReport == "0" ? "disabled" : "enabled"}")
	log.warn("default fade time is: ${defaultFadeTime}s") 
	log.warn("minimum brightness value is: ${minimumBrightnessValue}%") 
	log.warn("maximum brightness value is: ${maximumBrightnessValue}%") 
	log.warn("mosfet driving type is: ${mosfetDrivingType == "0" ? "trailing edge" : "leading edge"}")	
	log.warn("switch type is: ${switchType == "0" ? "momentary" : "toggle"}")
	log.warn("switch inclusion is: ${switchInclusion == "0" ? "disabled" : "enabled"}")
	log.warn("overcurrent protection is: ${overCurrentProtection == "0" ? "disabled" : "enabled"}")
	log.warn("power report change watts is: ${powerReportChangeWatts}W") 
	log.warn("power report change percent is: ${powerReportChangePercent}%") 
	log.warn("power report time is: ${powerReportTime}s") 
	log.warn("voltage report time is: ${voltageReportTime}s")  
	log.warn("current report time is: ${currentReportTime}s")
    log.warn("energy report time is: ${energyReportTime}s") 
	log.warn("dimming curve is: ${dimmingCurve == "0" ? "linear" : "logarithmic"}") 
	log.warn("startup brightness is: ${startupBrightness}%") 
	state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def configure() {
	logDebug("configure called")
	def cmds = commands([
						zwave.configurationV1.configurationGet(parameterNumber: 2), 
						zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: powerFailState.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 2), 
						zwave.configurationV1.configurationGet(parameterNumber: 3), 
						zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: basicReport.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 3), 
						zwave.configurationV1.configurationGet(parameterNumber: 4), 
						zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: defaultFadeTime.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 4), 
						zwave.configurationV1.configurationGet(parameterNumber: 5), 
						zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: minimumBrightnessValue.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 5), 
						zwave.configurationV1.configurationGet(parameterNumber: 6), 
						zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: maximumBrightnessValue.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 6), 
						zwave.configurationV1.configurationGet(parameterNumber: 7), 
						zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: mosfetDrivingType.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 7), 
						zwave.configurationV1.configurationGet(parameterNumber: 8), 
						zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: switchType.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 8), 
						zwave.configurationV1.configurationGet(parameterNumber: 9), 
						zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: switchInclusion.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 9), 
						zwave.configurationV1.configurationGet(parameterNumber: 13), 
						zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: overCurrentProtection.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 13), 
						zwave.configurationV1.configurationGet(parameterNumber: 14), 
						zwave.configurationV1.configurationSet(parameterNumber: 14, size: 2, scaledConfigurationValue: powerReportChangeWatts.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 14), 
						zwave.configurationV1.configurationGet(parameterNumber: 15), 
						zwave.configurationV1.configurationSet(parameterNumber: 15, size: 1, scaledConfigurationValue: powerReportChangePercent.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 15), 
						zwave.configurationV1.configurationGet(parameterNumber: 21), 
						zwave.configurationV1.configurationSet(parameterNumber: 21, size: 4, scaledConfigurationValue: powerReportTime.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 21), 
						zwave.configurationV1.configurationGet(parameterNumber: 23), 
						zwave.configurationV1.configurationSet(parameterNumber: 23, size: 4, scaledConfigurationValue: voltageReportTime.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 23), 
						zwave.configurationV1.configurationGet(parameterNumber: 24), 
						zwave.configurationV1.configurationSet(parameterNumber: 24, size: 4, scaledConfigurationValue: currentReportTime.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 24), 
						zwave.configurationV1.configurationGet(parameterNumber: 22), 
						zwave.configurationV1.configurationSet(parameterNumber: 22, size: 4, scaledConfigurationValue: energyReportTime.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 22), 
						zwave.configurationV1.configurationGet(parameterNumber: 31), 
						zwave.configurationV1.configurationSet(parameterNumber: 31, size: 1, scaledConfigurationValue: dimmingCurve.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 31),
						zwave.configurationV1.configurationGet(parameterNumber: 32), 
						zwave.configurationV1.configurationSet(parameterNumber: 32, size: 1, scaledConfigurationValue: startupBrightness.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 32)
						], 
						1000)
	logDebug("sending ${cmds}")
	return cmds            
}

def refresh() {
	logDebug("refresh called")
	def cmds = commands([
						zwave.basicV1.basicGet(), 
						zwave.meterV3.meterGet(scale: 0), 
						zwave.meterV3.meterGet(scale: 2), 
						zwave.meterV3.meterGet(scale: 4), 
						zwave.meterV3.meterGet(scale: 5)
						],
						500)
	logDebug("sending ${cmds}")
	return cmds
}

def parse(String description) {
	logDebug("parse called")
	logDebug("got description: ${description}")	
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	} 
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    logDebug("hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation called")
	logDebug("got cmd: ${cmd}")
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
    else {
		log.warn("Unable to extract encapsulated cmd from ${cmd}")
	}
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd){
	logDebug("zwaveEvent hubitat.zwave.commands.supervisionv1.SupervisionGet called")
	logDebug("got cmd: ${cmd}")
	hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
	else {
		log.warn("Unable to extract encapsulated cmd from ${cmd}")
	}
	sendHubCommand(new hubitat.device.HubAction(zwaveSecureEncap(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.basicv1.BasicReport called")
	logDebug("got cmd: ${cmd}")
	def switchValue = (cmd.value == 0 ? "off" : "on")
	def descriptionText = "${device.displayName} was turned ${switchValue}"
	if (device.currentValue("switch") && switchValue == device.currentValue("switch")) {
		descriptionText = "${device.displayName} is ${switchValue}"
	}                
	def type = "physical"
	if (state["action"] == "digitalon" || state["action"] == "digitaloff" || state["action"] == "digitalsetlevel") {
		logDebug("action is ${state["action"]}")
		type = "digital"
		state["action"] = "unknown"
	}
	logText(descriptionText)
	sendEvent(getEvent([name: "switch", value: switchValue, type: type, descriptionText: descriptionText]))
	def levelValue = cmd.value == 99 ? 100 : cmd.value
	descriptionText = "${device.displayName} was set to ${levelValue}%"
	if (device.currentValue("level") && levelValue == device.currentValue("level")) {
		descriptionText = "${device.displayName} is ${levelValue}%"
	}
	logText(descriptionText)
	sendEvent(getEvent(name: "level", value: levelValue, type: type, unit: "%", descriptionText: descriptionText))
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.meterv3.MeterReport called")
	logDebug("got: ${cmd}")
	def descriptionText = ""
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			logDebug("energy report is ${cmd.scaledMeterValue} kWh")
			descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}kWh"
			sendEvent(getEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh", descriptionText: descriptionText))
		} 
		else if (cmd.scale == 2) {
			logDebug("power report is ${cmd.scaledMeterValue} W")
			descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}W"
			sendEvent(getEvent(name: "power", value: cmd.scaledMeterValue, unit: "W", descriptionText: descriptionText))
		}
		else if (cmd.scale == 4) {
			logDebug("voltage report is ${cmd.scaledMeterValue} V")
			descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}V"
			sendEvent(getEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V", descriptionText: descriptionText))
		}
		else if (cmd.scale == 5) {
			logDebug("current report is ${cmd.scaledMeterValue} A")
			descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}A"
			sendEvent(getEvent(name: "amperage", value: cmd.scaledMeterValue, unit: "A", descriptionText: descriptionText))
		}
		else {
			log.warn("skipped cmd: ${cmd}")
		}
		if (descriptionText != "") {
			logText(descriptionText)
		}
	}
}

def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.notificationv8.NotificationReport called")
	logDebug("got cmd: ${cmd}")
	def deviceNotification = ""
    if (cmd.notificationType == 9) {
        logDebug("got system notification event: ${cmd.event}")
        switch (cmd.event) {
            case 7:
                // emergency shutoff 
				deviceNotification = "temperature exceeds device limit, emergency shutoff triggered!"
                log.warn(deviceNotification)				
                break
            default:
				log.warn("skipped cmd: ${cmd}")
        }
	}
	else if (cmd.notificationType == 8) {
        logDebug("got power management notification event: ${cmd.event}")
        switch (cmd.event) {
            case 5:
				//voltage drop / drift
				deviceNotification = "voltage drop / drift detected!"
                log.warn(deviceNotification)				
                break
			case 6:
				//over-current
				deviceNotification = "over-current detected!"
                log.warn(deviceNotification)				
                break
            default:
				log.warn("skipped cmd: ${cmd}")
        }
	}
    else {
        log.warn("skipped cmd: ${cmd}")
    }
	if (deviceNotification != "") {
		def descriptionText = "${device.displayName} raised notifiction - ${deviceNotification}"
		sendEvent(getEvent(name: "deviceNotification", value: deviceNotification, descriptionText: descriptionText))
	}
}

def zwaveEvent(hubitat.zwave.Command cmd) {
	logDebug("zwaveEvent hubitat.zwave.Command called")
	log.warn("skipped cmd: ${cmd}")
}

def on() {
	logDebug("on called")
	def cmds = commands(zwave.basicV1.basicSet(value: 0xFF))
	logDebug("sending ${cmds}")
	state["action"] = "digitalon"
	return cmds
}

def off() {
	logDebug("off called")
	def cmds = commands(zwave.basicV1.basicSet(value: 0x00))
	logDebug("sending ${cmds}")
	state["action"] = "digitaloff"
	return cmds
}

def setLevel(level, duration = 1) {
	logDebug("setLevel called")
	if (level > 99) level = 99
	if (level < 0) level = 99
	def cmds = commands(zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration))
	logDebug("sending ${cmds}")
	state["action"] = "digitalsetlevel"
	return cmds
}

def startLevelChange(direction, duration = 30) {
	logDebug("startLevelChange called")
	def upDown = direction == "down"	
	def cmds = zwaveSecureEncap(zwave.switchMultilevelV3.switchMultilevelStartLevelChange(ignoreStartLevel: true, incDec:3, startLevel:0, stepSize:0, dimmingDuration: duration, upDown: upDown))
	logDebug("sending ${cmds}")
	state["action"] = "digitalsetlevel"
	return cmds
}

def stopLevelChange() {
	logDebug("stopLevelChange called")
	def cmds = zwaveSecureEncap(zwave.switchMultilevelV3.switchMultilevelStopLevelChange())
	logDebug("sending ${cmds}")
	state["action"] = "digitalsetlevel"
	return cmds
}

def getEvent(event) {
    logDebug("getEvent called data: ${event}")
    return createEvent(event)
}

def commands(java.util.ArrayList cmds, delay = 200) {	
	return delayBetween(cmds.collect { secure(it) }, delay)
}

def commands(hubitat.zwave.Command cmd) {
	return secure(cmd)
}

def commands(String cmd) {
	return secure(cmd)
}

def secure(String cmd){
    logDebug("secure called cmd: ${cmd}")
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def secure(hubitat.zwave.Command cmd){
    logDebug("secure called cmd: ${cmd}")
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def secure(java.util.ArrayList cmds){
    return cmds.collect { secure(it) }
}

def logDebug(msg) {
	if (logEnable != false) {
		log.debug("${msg}")
	}
}

def logText(msg) {
	if (txtEnable != false) {
		log.info("${msg}")
	}
}

def logsOff() {
    log.warn("debug logging disabled")
    device.updateSetting("logEnable", [value:"false", type: "bool"])
}