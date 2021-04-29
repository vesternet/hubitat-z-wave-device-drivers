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

		fingerprint mfr: "0330", prod: "0200", deviceId: "D00C", inClusters:"0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-DIM-001 2-Wire Capable Dimmer"
	}
	preferences {
		input name: "powerFailState", type: "enum", title: "Load State After Power Failure", options: [0: "off", 1: "on", 2: "previous state"], defaultValue: 2
		input name: "switchType", type: "enum", title: "Switch Type Attached", options: [0: "momentary", 1: "toggle"], defaultValue: 0		
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
    device.updateSetting("switchType", [value: "0", type: "enum"])
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")
	log.warn("debug logging is: ${logEnable == true}")
	log.warn("power fail state is: ${powerFailState == "0" ? "off" : powerFailState == "1" ? "on" : "previousstate"}")
	log.warn("switch type is: ${switchType == "0" ? "momentary" : "toggle"}")
	state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def configure() {
	logDebug("configure called")
	def cmds = commands([zwave.configurationV1.configurationGet(parameterNumber: 2), zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: powerFailState.toInteger()), zwave.configurationV1.configurationGet(parameterNumber: 2), zwave.configurationV1.configurationGet(parameterNumber: 8), zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: switchType.toInteger()), zwave.configurationV1.configurationGet(parameterNumber: 8)], 1000)
	logDebug("sending ${cmds}")
	return cmds            
}

def refresh() {
	logDebug("refresh called")
	def cmds = commands([zwave.basicV1.basicGet(), zwave.meterV3.meterGet(scale: 0), zwave.meterV3.meterGet(scale: 2), zwave.meterV3.meterGet(scale: 4), zwave.meterV3.meterGet(scale: 5)])
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
	sendEvent(getEvent([name: "switch", value: switchValue, type: type, descriptionText: descriptionText]))
	def levelValue = cmd.value == 99 ? 100 : cmd.value
	descriptionText = "${device.displayName} was set to ${levelValue}%"
	if (device.currentValue("level") && levelValue == device.currentValue("level")) {
		descriptionText = "${device.displayName} is ${levelValue}%"
	}
	sendEvent(getEvent(name: "level", value: levelValue, type: type, unit: "%", descriptionText: descriptionText))
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.meterv3.MeterReport called")
	logDebug("got: ${cmd}")
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			logDebug("energy report is ${cmd.scaledMeterValue} kWh")
			sendEvent(getEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh", descriptionText: "${device.displayName} is set to ${cmd.scaledMeterValue}kWh"))
		} 
		else if (cmd.scale == 2) {
			logDebug("power report is ${cmd.scaledMeterValue} W")
			sendEvent(getEvent(name: "power", value: cmd.scaledMeterValue, unit: "W", descriptionText: "${device.displayName} is set to ${cmd.scaledMeterValue}W"))
		}
		else if (cmd.scale == 4) {
			logDebug("voltage report is ${cmd.scaledMeterValue} V")
			sendEvent(getEvent(name: "voltage", value: cmd.scaledMeterValue, unit: "V", descriptionText: "${device.displayName} is set to ${cmd.scaledMeterValue}V"))
		}
		else if (cmd.scale == 5) {
			logDebug("current report is ${cmd.scaledMeterValue} A")
			sendEvent(getEvent(name: "current", value: cmd.scaledMeterValue, unit: "A", descriptionText: "${device.displayName} is set to ${cmd.scaledMeterValue}A"))
		}
		else {
			log.warn("skipped cmd: ${cmd}")
		}
	}
}

def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd) {
	logDebug("zwaveEvent hubitat.zwave.commands.notificationv8.NotificationReport called")
	logDebug("got cmd: ${cmd}")
    if (cmd.notificationType == 9) {
        logDebug("got system notification event: ${cmd.event}")
        switch (cmd.event) {
            case 7:
                // emergency shutoff 
                log.warn("temperature exceeds device limit, emergency shutoff triggered!")
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
                log.warn("voltage drop / drift detected!")
                break
			case 6:
				//over-current
                log.warn("over-current detected!")
                break
            default:
				log.warn("skipped cmd: ${cmd}")
        }
	}
    else {
        log.warn("skipped cmd: ${cmd}")
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

def logDebug(msg) {
	if (logEnable != false) {
		log.debug("${msg}")
	}
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

def logsOff() {
    log.warn("debug logging disabled")
    device.updateSetting("logEnable", [value:"false", type: "bool"])
}