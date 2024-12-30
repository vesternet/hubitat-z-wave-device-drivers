/**
 *	Vesternet VES-ZW-HLD-016 High Load Switch
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZW-HLD-016 High Load Switch", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20VES-ZW-HLD-016%20High%20Load%20Switch.groovy") {
		capability "Switch"
		capability "Actuator"
		capability "Sensor"
		capability "PowerMeter"
		capability "EnergyMeter"
		capability "VoltageMeasurement"	
		capability "CurrentMeter"	
		capability "Refresh"	
		capability "Configuration"

		attribute "deviceNotification", "string"

		fingerprint mfr: "0330", prod: "0004", deviceId: "D019", inClusters:"0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-HLD-016 High Load Switch"		
	}
	preferences {
		input name: "powerFailState", type: "enum", title: "Load State After Power Failure", options: [0: "off", 1: "on", 2: "previous state"], defaultValue: 2
		input name: "basicReport", type: "enum", title: "Send Basic Report", options: [0: "disabled", 1: "enabled"], defaultValue: 1
		input name: "overCurrentProtection", type: "enum", title: "Overcurrent Protection (over 16A or 3700W)", options: [0: "disabled", 1: "enabled"], defaultValue: 1
		input name: "switchType", type: "enum", title: "Switch Type Attached", options: [0: "momentary", 1: "toggle"], defaultValue: 0
		input name: "switchInclusion", type: "enum", title: "Allow Switch Input To Include / Exclude", options: [0: "disabled", 1: "enabled"], defaultValue: 1
		input name: "powerReportChangeWatts", type: "number", title: "Power Change Watts (1W - 255W, 0 = disabled)", range: "0..255", defaultValue: 5
		input name: "voltageReportChangeVolts", type: "number", title: "Voltage Change Volts (1V - 255V, 0 = disabled)", range: "0..255", defaultValue: 2         
		input name: "currentReportChangeAmps", type: "number", title: "Current Change Amps (0.1A - 25.5A, 0 = disabled)", range: "0..255", defaultValue: 1    		
		input name: "reportTime", type: "enum", title: "Power, Voltage, Current & Energy Report Time (s)", options: [0: "disabled",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s",1800:"1800s",3600:"3600s",7200:"7200s"], defaultValue: 1800
		input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
		input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
	}
}

def getCommandClassVersions() {
	[ 0x20: 1, 0x25:2, 0x32: 5, 0x70: 1, 0x71: 8 ]
}

def installed() {	
	device.updateSetting("logEnable", [value: "true", type: "bool"])
	logDebug("installed called")
	device.updateSetting("powerFailState", [value: "2", type: "enum"])
	device.updateSetting("basicReport", [value: "1", type: "enum"])
	device.updateSetting("overCurrentProtection", [value: "1", type: "enum"])
    device.updateSetting("switchType", [value: "0", type: "enum"])
	device.updateSetting("switchInclusion", [value: "1", type: "enum"])
	device.updateSetting("powerReportChangeWatts", [value: 5, type: "number"])
	device.updateSetting("voltageReportChangeVolts", [value: 2, type: "number"])
	device.updateSetting("currentReportChangeAmps", [value: 1, type: "number"])
	device.updateSetting("reportTime", [value: "1800", type: "enum"])
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")
	log.warn("debug logging is: ${logEnable == true}")
	log.warn("power fail state is: ${powerFailState == "0" ? "off" : powerFailState == "1" ? "on" : "previousstate"}")
	log.warn("basic report is: ${basicReport == "0" ? "disabled" : "enabled"}")
	log.warn("overcurrent protection is: ${overCurrentProtection == "0" ? "disabled" : "enabled"}")
	log.warn("switch type is: ${switchType == "0" ? "momentary" : "toggle"}")
	log.warn("switch inclusion is: ${switchInclusion == "0" ? "disabled" : "enabled"}")
	log.warn("power report change watts is: ${powerReportChangeWatts}W") 
	log.warn("voltage report change volts is: ${voltageReportChangeVolts}V") 
	log.warn("current report change amps is: ${currentReportChangeAmps == 0 ?: currentReportChangeAmps / 10}A") 
	log.warn("power, voltage, current & energy report time is: ${reportTime}s") 
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
						zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: overCurrentProtection.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 4), 
						zwave.configurationV1.configurationGet(parameterNumber: 5), 
						zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: switchType.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 5),
						zwave.configurationV1.configurationGet(parameterNumber: 7), 
						zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: switchInclusion.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 7), 
						zwave.configurationV1.configurationGet(parameterNumber: 10), 
						zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: powerReportChangeWatts.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 10), 
						zwave.configurationV1.configurationGet(parameterNumber: 11), 
						zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: currentReportChangeAmps.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 11), 
						zwave.configurationV1.configurationGet(parameterNumber: 12), 
						zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: voltageReportChangeVolts.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 12), 
						zwave.configurationV1.configurationGet(parameterNumber: 13), 
						zwave.configurationV1.configurationSet(parameterNumber: 13, size: 4, scaledConfigurationValue: reportTime.toInteger()), 
						zwave.configurationV1.configurationGet(parameterNumber: 13), 
						],
						 1000)
	logDebug("sending ${cmds}")
	return cmds            
}

def refresh() {
	logDebug("refresh called")
	def cmds = commands([
						zwave.basicV1.basicGet(), 
						zwave.meterV5.meterGet(scale: 0), 
						zwave.meterV5.meterGet(scale: 2), 
						zwave.meterV5.meterGet(scale: 4), 
						zwave.meterV5.meterGet(scale: 5)
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
	sendHubCommand(new hubitat.device.HubAction(commands(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.basicv1.BasicReport called")
	logDebug("got cmd: ${cmd}")
	def switchValue = cmd.value == 0 ? "off" : "on"
	def descriptionText = "${device.displayName} was turned ${switchValue}"
	if (device.currentValue("switch") && switchValue == device.currentValue("switch")) {
		descriptionText = "${device.displayName} is ${switchValue}"
	}                
	def type = "physical"
	if (state["action"] == "digitalon" || state["action"] == "digitaloff") {
		logDebug("action is ${state["action"]}")
		type = "digital"
		state["action"] = "standby"
	}
	logText(descriptionText)
	sendEvent(getEvent([name: "switch", value: switchValue, type: type, descriptionText: descriptionText]))
}

def zwaveEvent(hubitat.zwave.commands.meterv5.MeterReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.meterv5.MeterReport called")
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
    if (cmd.notificationType == 8) {
        logDebug("got power management notification event: ${cmd.event}")
        switch (cmd.event) {
            case 6:
                // overcurrent detected
				deviceNotification = "current exceeds device limit, emergency shutoff triggered!"
                log.warn(deviceNotification)	
                break
			case 8:
                // overload detected
				deviceNotification = "load exceeds device limit, emergency shutoff triggered!"
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