/**
 *	Vesternet VES-ZW-WAL-008 2 Zone Wall Controller
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZW-WAL-008 2 Zone Wall Controller", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20VES-ZW-WAL-008%202%20Zone%20Wall%20Controller.groovy") {		
        capability "PushableButton"
        capability "HoldableButton"
        capability "ReleasableButton"
		capability "Battery"
		capability "Sensor"
        capability "Configuration"

		fingerprint mfr: "0330", prod: "0300", deviceId: "A306", inClusters:"0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-WAL-008 2 Zone Wall Controller"
	}
	preferences {
		input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
		input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
		input name: "doConfigure", type: "bool", title: "Carry out device configuration when it next wakes up", defaultValue: true
	}
}

def getCommandClassVersions() {
	[ 0x5B: 3, 0x70: 1, 0x80: 1, 0x84: 2 ]
}

def getModelNumberOfButtons() {
    logDebug("getModelNumberOfButtons called")
    ["41734" : 4]
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
	log.warn("configure is: ${doConfigure == true}")
	state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def configure() {
	logDebug("configure called")
	log.warn("battery powered device, wake it up manually to configure it or wait for the wake up interval!")
	device.updateSetting("doConfigure", [value: "true", type: "bool"])
}

def wakeUpConfig() {
	logDebug("wakeUpConfig called")
	def cmds = [ zwave.wakeUpV2.wakeUpIntervalGet(),
				zwave.wakeUpV2.wakeUpIntervalSet(seconds: 28800, nodeid: zwaveHubNodeId),
				zwave.wakeUpV2.wakeUpIntervalGet() ]
	logDebug("returns cmds: ${cmds}")				
	return cmds
}

def refreshBattery() {
	logDebug("refreshBattery called")
	def cmds = [ zwave.batteryV1.batteryGet() ]
	logDebug("returns cmds: ${cmds}")
	return cmds
}

def configureDevice() {
	logDebug("configureDevice called doConfigure: ${doConfigure}")	
	if (doConfigure) {
		def cmds = commands(wakeUpConfig() + zwave.wakeUpV2.wakeUpNoMoreInformation(), 2000)
		device.updateSetting("doConfigure", [value: "false", type: "bool"])
	   	logDebug("sending ${cmds}")
		sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZWAVE))
	}
	else {
		refreshDevice()
	}
}

def refreshDevice() {
	logDebug("refreshDevice called")	
	def cmds = commands(refreshBattery() + zwave.wakeUpV2.wakeUpNoMoreInformation())
   	logDebug("sending ${cmds}")
	sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZWAVE))	
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

def zwaveEvent(hubitat.zwave.commands.centralscenev3.CentralSceneNotification cmd){
    logDebug("zwaveEvent hubitat.zwave.commands.centralscenev3.CentralSceneNotification called")
	logDebug("got cmd: ${cmd}")
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
		default:
            log.warn("skipped cmd: ${cmd}")
            break
    }
    if (action){
        sendEvent(getButtonEvent(action, button, "physical"))
    }    
}

def zwaveEvent(hubitat.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    logDebug("zwaveEvent hubitat.zwave.commands.wakeupv2.WakeUpNotification called")
	logDebug("got cmd: ${cmd}")
    runIn(1,configureDevice)
}

def zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport cmd) {
    logDebug("zwaveEvent hubitat.zwave.commands.batteryv1.BatteryReport called")
	logDebug("got cmd: ${cmd}")
	def batteryLevel = cmd.batteryLevel
	def descriptionText = "${device.displayName} battery level is ${batteryLevel}%"
	if (cmd.batteryLevel == 0xFF) {
		batteryLevel = 1
		descriptionText = "${device.displayName} battery level is low!"
	} 	
	logText(descriptionText)	                          
	sendEvent(getEvent(name: "battery", value: batteryLevel, unit: "%", descriptionText: descriptionText))
}

def zwaveEvent(hubitat.zwave.Command cmd) {
	logDebug("zwaveEvent hubitat.zwave.Command called")
	log.warn("skipped cmd: ${cmd}")
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

def getButtonEvent(action, button, type) {
    logDebug("getButtonEvent called button: ${button} action: ${action} type: ${type} ")    
	def descriptionText = "${device.displayName} button ${button} is ${action}"
	logText(descriptionText)
	return getEvent(name: action, value: button, descriptionText: descriptionText, isStateChange: true, type: type)
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