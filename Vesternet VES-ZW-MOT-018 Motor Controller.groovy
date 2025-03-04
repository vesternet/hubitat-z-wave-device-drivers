/**
 *    Vesternet VES-ZW-MOT-018 Motor Controller
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
        command "startCalibration"

        attribute "deviceNotification", "string"
        
        fingerprint mfr: "0330", prod: "0004", deviceId: "D00D", inClusters: "0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-MOT-018 Motor Controller"                      
    }
    preferences {
        input name: "stateChangePercentageReport", type: "number", title: "State Change Percentage Report (1% - 10%, 0 = disabled)", range: "0..10", defaultValue: 5
        input name: "workingMode", type: "enum", title: "Working Mode", options: [0: "light mode", 1: "shutter mode without positioning", 2: "shutter mode with positioning"], defaultValue: 2
        input name: "savePositioningPercentage", type: "enum", title: "Save Positioning Percentage", options: [0: "disabled", 1: "enabled"], defaultValue: 1
        input name: "overCurrentProtection", type: "enum", title: "Overcurrent Protection (over 4.1A resistive, over 2.1A motor or capacitive)", options: [0: "disabled", 1: "enabled"], defaultValue: 1
        input name: "localSwitchControlOption", type: "number", title: "Configuration Of Switch Control (Mode 0 - 4, 0 = default, see manual for options)", range: "0..4", defaultValue: 0
        input name: "switchInclusion", type: "enum", title: "Allow Switch Input To Include / Exclude", options: [0: "disabled", 1: "enabled"], defaultValue: 1
        input name: "centralSceneNotification", type: "enum", title: "Central Scene Configuration", options: [0: "disabled", 1: "enabled for both inputs", 2: "enabled for input S1", 3: "enabled for input S2"], defaultValue: 1
        input name: "powerReportChangeWatts", type: "number", title: "Power Change Watts (1W - 255W, 0 = disabled)", range: "0..255", defaultValue: 5
        input name: "voltageReportChangeVolts", type: "number", title: "Voltage Change Volts (1V - 255V, 0 = disabled)", range: "0..255", defaultValue: 2         
        input name: "currentReportChangeAmps", type: "number", title: "Current Change Amps (0.1A - 25.5A, 0 = disabled)", range: "0..255", defaultValue: 1
        input name: "slatsRotationTime", type: "number", title: "Slats Rotation Time (0.5s - 25s, 0 = disabled)", range: "0..250", defaultValue: 0
        input name: "energyReportTime", type: "enum", title: "Energy Time (s)", options: [0: "disabled",10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s",1800:"1800s",3600:"3600s",7200:"7200s"], defaultValue: 1800
        

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
    device.updateSetting("stateChangePercentageReport", [value: 5, type: "number"])
    device.updateSetting("workingMode", [value: "2", type: "enum"])    
    device.updateSetting("savePositioningPercentage", [value: "1", type: "enum"])
    device.updateSetting("overCurrentProtection", [value: "1", type: "enum"])
    device.updateSetting("localSwitchControlOption", [value: 0, type: "number"])
    device.updateSetting("switchInclusion", [value: "1", type: "enum"])
    device.updateSetting("centralSceneNotification", [value: "1", type: "enum"])
    device.updateSetting("powerReportChangeWatts", [value: 5, type: "number"])
    device.updateSetting("voltageReportChangeVolts", [value: 2, type: "number"])
    device.updateSetting("currentReportChangeAmps", [value: 1, type: "number"])
    device.updateSetting("slatsRotationTime", [value: 0, type: "number"])
    device.updateSetting("energyReportTime", [value: "1800", type: "enum"])
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
    log.warn("state change percentage report is: ${stateChangePercentageReport}%")
    log.warn("working mode is: ${workingMode == "0" ? "light mode" : workingMode == "1" ? "shutter mode without positioning" : "shutter mode with positioning"}")    
    log.warn("save positioning percentage is: ${savePositioningPercentage == "0" ? "disabled" : "enabled"}")
    log.warn("overcurrent protection is: ${overCurrentProtection == "0" ? "disabled" : "enabled"}")
    log.warn("local switch control option is: ${localSwitchControlOption}")
    log.warn("switch inclusion is: ${switchInclusion == "0" ? "disabled" : "enabled"}")
    log.warn("central scene notification is: ${centralSceneNotification}")
    log.warn("power report change watts is: ${powerReportChangeWatts}W") 
    log.warn("voltage report change volts is: ${voltageReportChangeVolts}V") 
    log.warn("current report change amps is: ${currentReportChangeAmps == 0 ?: currentReportChangeAmps / 10}A") 
    log.warn("slats rotation time is: ${slatsRotationTime == 0 ?: slatsRotationTime / 10}s") 
    log.warn("energy report time is: ${energyReportTime}s") 
    state.clear()
    unschedule()
    if (logEnable) runIn(1800,logsOff)
}

def configure() {
    logDebug("configure called")     
    def cmds = commands([
                        zwave.configurationV1.configurationGet(parameterNumber: 2), 
                        zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: stateChangePercentageReport.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 2),
                        zwave.configurationV1.configurationGet(parameterNumber: 3), 
                        zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: workingMode.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 3),
                        zwave.configurationV1.configurationGet(parameterNumber: 4), 
                        zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: savePositioningPercentage.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 4), 
                        zwave.configurationV1.configurationGet(parameterNumber: 5), 
                        zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: overCurrentProtection.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 5),
                        zwave.configurationV1.configurationGet(parameterNumber: 7), 
                        zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: localSwitchControlOption.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 7), 
                        zwave.configurationV1.configurationGet(parameterNumber: 8), 
                        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: switchInclusion.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 8), 
                        zwave.configurationV1.configurationGet(parameterNumber: 9), 
                        zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: centralSceneNotification.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 9),
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
                        zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: slatsRotationTime.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 13),
                        zwave.configurationV1.configurationGet(parameterNumber: 14), 
                        zwave.configurationV1.configurationSet(parameterNumber: 14, size: 4, scaledConfigurationValue: energyReportTime.toInteger()), 
                        zwave.configurationV1.configurationGet(parameterNumber: 14)
                        ],
                        1000)
    logDebug("sending ${cmds}")
    return cmds 
}

def startCalibration() {
    logDebug("startCalibration called")
    def cmds = commands([
                        zwave.configurationV1.configurationGet(parameterNumber: 6), 
                        zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: 1), 
                        zwave.configurationV1.configurationGet(parameterNumber: 6),
                        ],
                        1000)
    logDebug("sending ${cmds}")
    return cmds
    //TODO - check that returned commands are executed, if not doZig
}

def refresh() {
    logDebug("refresh called")    
    def cmds = commands([
                        zwave.basicV1.basicGet(), 
                        zwave.switchMultilevelV3.switchMultilevelGet(), 
                        zwave.meterV3.meterGet(scale: 0), 
                        zwave.meterV3.meterGet(scale: 2), 
                        zwave.meterV3.meterGet(scale: 4), 
                        zwave.meterV3.meterGet(scale: 5)
                        ],
                        500)
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
    def deviceNotification = ""
    if (endpoint == 0) {
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
    }
    else {
        logDebug("got command from unexpected endpoint, skipping!")
    }
    if (deviceNotification != "") {
        def descriptionText = "${device.displayName} raised notifiction - ${deviceNotification}"
        sendEvent(getEvent(name: "deviceNotification", value: deviceNotification, descriptionText: descriptionText))
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