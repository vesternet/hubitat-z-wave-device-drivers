/**
 *	Vesternet VES-ZW-MOT-018 Motor Controller
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZW-MOT-018 Motor Controller", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20VES-ZW-MOT-018%20Motor%20Controller.groovy", singleThreaded: true) {
		capability "Actuator"
        capability "WindowShade"
        capability "Sensor"
        capability "PowerMeter"
		capability "EnergyMeter"
		capability "VoltageMeasurement"	
		capability "CurrentMeter"
		capability "PushableButton"
		capability "DoubleTapableButton"
        capability "HoldableButton"
        capability "ReleasableButton"
		capability "Configuration"
        capability "Refresh"

        command "tripleTap", [ [ name:"Triple Tap*", type: "NUMBER", description: "Button number to triple tap", required: true ] ]
		
        fingerprint mfr: "0330", prod: "0004", deviceId: "D00D", inClusters: "0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-MOT-018 Motor Controller"                      
	}
	preferences {
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
	}
}

def getCommandClassVersions() {
	[ 0x20: 1, 0x25: 1, 0x32: 3, 0x60: 4, 0x70: 1, 0x71: 8, 0x5B: 3 ]//multilevel 0x26 v4 on device
}

def getModelNumberOfButtons() {
    logDebug("getModelNumberOfButtons called")
    ["53261" : 2]
}

def installed() {
    device.updateSetting("logEnable", [value: "true", type: "bool"])
    device.updateSetting("txtEnable", [value: "true", type: "bool"])
    logDebug("installed called")	
    def numberOfButtons = modelNumberOfButtons[device.getDataValue("deviceId")]
    logDebug("numberOfButtons: ${numberOfButtons}")
    sendEvent(getEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false))
    for(def buttonNumber : 1..numberOfButtons) {		
        sendEvent(getButtonEvent("pushed", buttonNumber, "digital"))
    }
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")
	log.warn("debug logging is: ${logEnable == true}")
	log.warn("descriptionText logging is: ${txtEnable == true}")
    state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def configure() {
	logDebug("configure called")     
}

def refresh() {
	logDebug("refresh called")    
	def cmds = commands([zwave.basicV1.basicGet(), zwave.switchMultilevelV3.switchMultilevelGet(), zwave.meterV3.meterGet(scale: 0), zwave.meterV3.meterGet(scale: 2), zwave.meterV3.meterGet(scale: 4), zwave.meterV3.meterGet(scale: 5)])
	logDebug("sending ${cmds}")
	return cmds
}

def open() {
    logDebug("open called")
    def cmds = commands(zwave.switchMultilevelV3.switchMultilevelSet(value: 99, dimmingDuration: 1))
	logDebug("sending ${cmds}")
    state["action"] = "digitalopen"    
    sendEvent(getEvent([name: "windowShade", value: "opening", type: "digital", descriptionText: "${device.displayName} is opening"]))
	return cmds    
}

def close() {
    logDebug("close called")
    def cmds = commands(zwave.switchMultilevelV3.switchMultilevelSet(value: 0, dimmingDuration: 1))
	logDebug("sending ${cmds}")
    state["action"] = "digitalclose"
    sendEvent(getEvent([name: "windowShade", value: "closing", type: "digital", descriptionText: "${device.displayName} is closing"]))
	return cmds
}

def setPosition(position) {
    logDebug("setPosition called")
    logDebug("got position: ${position}")
    if (position > 99) position = 99
	if (position < 0) position = 99
	def cmds = commands(zwave.switchMultilevelV3.switchMultilevelSet(value: position, dimmingDuration: 1))
	logDebug("sending ${cmds}")
    state["action"] = "digitalsetposition"
    def currentValue =  device.currentValue("position") ?: "unknown"
    if (position < currentValue) {
        sendEvent(getEvent([name: "windowShade", value: "closing", type: "digital", descriptionText: "${device.displayName} is closing"]))
    }  
    else if (position > currentValue) {
        sendEvent(getEvent([name: "windowShade", value: "opening", type: "digital", descriptionText: "${device.displayName} is opening"]))
    }  
	return cmds
}

def startPositionChange(direction) {
    logDebug("startPositionChange called")
    logDebug("got direction: ${direction}")
	def cmds = zwaveSecureEncap(zwave.switchMultilevelV3.switchMultilevelStartLevelChange(ignoreStartLevel: true, incDec:3, startLevel:0, stepSize:0, dimmingDuration: 1, upDown: upDown))
	logDebug("sending ${cmds}")
    state["action"] = "digitalstartpositionchange"
    def currentValue =  device.currentValue("position") ?: "unknown"
    if (direction == "down") {
        sendEvent(getEvent([name: "windowShade", value: "closing", type: "digital", descriptionText: "${device.displayName} is closing"]))
    }  
    else if (direction == "up") {
        sendEvent(getEvent([name: "windowShade", value: "opening", type: "digital", descriptionText: "${device.displayName} is opening"]))
    }  
	return cmds
}

def stopPositionChange() {
    logDebug("stopPositionChange called")
    def cmds = zwaveSecureEncap(zwave.switchMultilevelV3.switchMultilevelStopLevelChange())
	logDebug("sending ${cmds}")
    state["action"] = "digitalstop"
    sendEvent(getEvent([name: "windowShade", value: "stopping", type: "digital", descriptionText: "${device.displayName} is stopping"]))
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
	zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, endpoint) {
    logDebug("hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, endpoint)
    }
    else {
		log.warn("Unable to extract encapsulated cmd from ${cmd}")
	}
}

def zwaveEvent(hubitat.zwave.commands.multichannelv4.MultiChannelCmdEncap cmd) {
	logDebug("hubitat.zwave.commands.multichannelv4.MultiChannelCmdEncap called")
	logDebug("got cmd: ${cmd}")
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		logDebug("got encapsulatedCommand: ${encapsulatedCommand} from endpoint: ${cmd.sourceEndPoint}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		log.warn("Unable to extract encapsulated cmd from ${cmd}")
	}
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd){
	zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd, endpoint) {
	logDebug("zwaveEvent hubitat.zwave.commands.supervisionv1.SupervisionGet called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
	hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, endpoint)
	}
	else {
		log.warn("Unable to extract encapsulated cmd from ${cmd}")
	}
	if (endpoint > 0) {	
		sendHubCommand(new hubitat.device.HubAction(commands(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0), endpoint), hubitat.device.Protocol.ZWAVE))
	}
	else {
		sendHubCommand(new hubitat.device.HubAction(commands(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
	}
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, endpoint) {
	logDebug("zwaveEvent hubitat.zwave.commands.basicv1.BasicReport called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    def levelValue = cmd.value == 99 ? 100 : cmd.value
    logDebug("current position is ${levelValue}")	                        
    def descriptionText = "${device.displayName} position was set to ${levelValue}%"
    def currentValue =  device.currentValue("position") ?: "unknown"
    if (levelValue == currentValue) {
        descriptionText = "${device.displayName} position is ${levelValue}%"
    }  
    def type = "physical"
    def action = state["action"] ?: "standby"
    if (action == "digitalsetposition") {
        logDebug("action is ${action}")
        type = "digital"
        state["action"] = "standby"
        logDebug("action set to standby")
    }
    logText(descriptionText)
    sendEvent(getEvent([name: "position", value: levelValue, unit: "%", type: type, descriptionText: descriptionText]))
    if (levelValue == 0) {
        sendEvent(getEvent([name: "windowShade", value: "closed", type: type, descriptionText: "${device.displayName} is closed"]))
    }
    else if (levelValue == 100) {
        sendEvent(getEvent([name: "windowShade", value: "open", type: type, descriptionText: "${device.displayName} is open"]))
    }
    else {
        sendEvent(getEvent([name: "windowShade", value: "partially open", type: type, descriptionText: "${device.displayName} is partially open"]))
    }
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, endpoint) {
	logDebug("zwaveEvent hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    def levelValue = cmd.value == 99 ? 100 : cmd.value
    logDebug("current position is ${levelValue}")	                        
    def descriptionText = "${device.displayName} position was set to ${levelValue}%"
    def currentValue =  device.currentValue("position") ?: "unknown"
    if (levelValue == currentValue) {
        descriptionText = "${device.displayName} position is ${levelValue}%"
    }  
    def type = "physical"
    def action = state["action"] ?: "standby"
    if (action == "digitalsetposition") {
        logDebug("action is ${action}")
        type = "digital"
        state["action"] = "standby"
        logDebug("action set to standby")
    }
    logText(descriptionText)
    sendEvent(getEvent([name: "position", value: levelValue, unit: "%", type: type, descriptionText: descriptionText]))
    if (levelValue == 0) {
        sendEvent(getEvent([name: "windowShade", value: "closed", type: type, descriptionText: "${device.displayName} is closed"]))
    }
    else if (levelValue == 100) {
        sendEvent(getEvent([name: "windowShade", value: "open", type: type, descriptionText: "${device.displayName} is open"]))
    }
    else {
        sendEvent(getEvent([name: "windowShade", value: "partially open", type: type, descriptionText: "${device.displayName} is partially open"]))
    }
}

def zwaveEvent(hubitat.zwave.commands.centralscenev3.CentralSceneNotification cmd){
	zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.centralscenev3.CentralSceneNotification cmd, endpoint){
    logDebug("zwaveEvent hubitat.zwave.commands.centralscenev3.CentralSceneNotification called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
	if (endpoint == 0) {
		Integer button = cmd.sceneNumber
		Integer key = cmd.keyAttributes
		String action
		switch (key) {
			case 0:
				action = "pushed"
				break
			case 1:	
				action = "released"
				break
			case 2:
				action = "held"
				break
			case 3:
				action = "doubleTapped"
				break
			case 4:
				action = "tripleTapped"//not supported on hubitat capability list
				break
			default:
				log.warn("skipped cmd: ${cmd}")
				break
		}
		if (action){
			sendEvent(getButtonEvent(action, button, "physical"))
		}    
	}
	else {
		logDebug("got command from unexpected endpoint, skipping!")
	}
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
	zwaveEvent(cmd, 0)
}	

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd, endpoint) {
	logDebug("zwaveEvent hubitat.zwave.commands.meterv3.MeterReport called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
	if (endpoint == 0) {
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
	else {
		logDebug("got command from unexpected endpoint, skipping!")
	}
}

def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd) {
	zwaveEvent(cmd, 0)
}	

def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd, endpoint) {
	logDebug("zwaveEvent hubitat.zwave.commands.notificationv8.NotificationReport called")
	logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
	if (endpoint == 0) {
		if (cmd.notificationType == 8) {
			logDebug("got power management notification event: ${cmd.event}")
			switch (cmd.event) {
				case 6:
					// overcurrent detected
					log.warn("current exceeds device limit, emergency shutoff triggered!")
					break
				case 8:
					// overload detected
					log.warn("load exceeds device limit, emergency shutoff triggered!")
					break
				default:
					log.warn("skipped cmd: ${cmd}")
			}
		}
		else {
			log.warn("skipped cmd: ${cmd}")
		}
	}
	else {
		logDebug("got command from unexpected endpoint, skipping!")
	}
}

def push(button){
    logDebug("push called")    
    sendEvent(getButtonEvent("pushed", button, "digital"))
}

def hold(button){
    logDebug("hold called")
    sendEvent(getButtonEvent("held", button, "digital"))
}

def release(button){
    logDebug("release called")
    sendEvent(getButtonEvent("released", button, "digital"))
}

def doubleTap(button){
    logDebug("doubleTap called")
    sendEvent(getButtonEvent("doubleTapped", button, "digital"))
}

def tripleTap(button){
    logDebug("tripleTap called")
    sendEvent(getButtonEvent("tripleTapped", button, "digital"))
}

def getButtonEvent(action, button, type) {
    logDebug("getButtonEvent called button: ${button} action: ${action} type: ${type} ")    
	def descriptionText = "${device.displayName} button ${button} is ${action}"
	logText(descriptionText)
	return getEvent(name: action, value: button, descriptionText: descriptionText, isStateChange: true, type: type)
}

def zwaveEvent(hubitat.zwave.Command cmd) {
	logDebug("zwaveEvent hubitat.zwave.Command called")
	log.warn("skipped cmd: ${cmd}")
}

def getEvent(event) {
    logDebug("getEvent called data: ${event}")
    return createEvent(event)
}

def commands(java.util.ArrayList cmds, delay = 200, endpoint = null) {	
	return delayBetween(cmds.collect { 
		if (it instanceof String || it instanceof hubitat.zwave.Command) {
			secure(it, endpoint)
		}
		else {
			secure(it.cmd, it.endpoint)
		}
		}, delay)
}

def commands(hubitat.zwave.Command cmd, endpoint = null) {
	return secure(cmd, endpoint)
}

def commands(String cmd, endpoint = null) {
	return secure(cmd, endpoint)
}

def secure(String cmd, endpoint = null){
    logDebug("secure called cmd: ${cmd} endpoint: ${endpoint}")
	if (endpoint) {
		if (endpoint instanceof String){
			endpoint = endpoint.toInteger()
		}
		cmd = zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 0, bitAddress: 0, res01:0, destinationEndPoint: endpoint).encapsulate(cmd)
	}
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def secure(hubitat.zwave.Command cmd, endpoint = null){
    logDebug("secure called cmd: ${cmd} endpoint: ${endpoint}")
	if (endpoint) {
		if (endpoint instanceof String){
			endpoint = endpoint.toInteger()
		}
		cmd = zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 0, bitAddress: 0, res01:0, destinationEndPoint: endpoint).encapsulate(cmd)
	}
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
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