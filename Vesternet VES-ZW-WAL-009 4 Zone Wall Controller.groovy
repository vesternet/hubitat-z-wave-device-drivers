/**
 *    Vesternet VES-ZW-WAL-009 4 Zone Wall Controller
 * 
 */
metadata {
    definition (name: "Vesternet VES-ZW-WAL-009 4 Zone Wall Controller", namespace: "Vesternet", author: "Vesternet", singleThreaded: true, importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20VES-ZW-WAL-009%204%20Zone%20Wall%20Controller.groovy") {        
        capability "PushableButton"
        capability "HoldableButton"
        capability "ReleasableButton"
        capability "Battery"
        capability "Sensor"
        capability "Configuration"

        command "associationsAddGroupTwo", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Adds an Association to Group 2 on this device.  Enter the Node ID for the target device.']]
        command "associationsRemoveGroupTwo", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Removes an Association to Group 2 on this device.  Enter the Node ID for the target device.']]
        command "associationsReadGroupTwo"
        command "associationsClearGroupTwo"
        command "associationsAddGroupThree", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Adds an Association to Group 3 on this device.  Enter the Node ID for the target device.']]
        command "associationsRemoveGroupThree", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Removes an Association to Group 3 on this device.  Enter the Node ID for the target device.']]
        command "associationsReadGroupThree"
        command "associationsClearGroupThree"
        command "associationsAddGroupFour", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Adds an Association to Group 4 on this device.  Enter the Node ID for the target device.']]
        command "associationsRemoveGroupFour", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Removes an Association to Group 4 on this device.  Enter the Node ID for the target device.']]
        command "associationsReadGroupFour"
        command "associationsClearGroupFour"
        command "associationsAddGroupFive", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Adds an Association to Group 5 on this device.  Enter the Node ID for the target device.']]
        command "associationsRemoveGroupFive", [[name:'Target Device Node ID*', type: 'NUMBER', description: 'Removes an Association to Group 5 on this device.  Enter the Node ID for the target device.']]
        command "associationsReadGroupFive"
        command "associationsClearGroupFive"

        fingerprint mfr: "0330", prod: "0300", deviceId: "A305", inClusters:"0x5E,0x55,0x98,0x9F,0x6C", deviceJoinName: "Vesternet VES-ZW-WAL-009 4 Zone Wall Controller"        
    }
    preferences {
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
        input name: "doConfigure", type: "bool", title: "Carry out device configuration when it next wakes up", defaultValue: true
        input name: "createChildDevices", type: "bool", title: "Create child devices for Switch / SwitchLevel capability Events", defaultValue: false
    }
}

def getCommandClassVersions() {
    [ 0x5B: 3, 0x70: 1, 0x80: 1, 0x84: 2 ]
}

def getModelNumberOfButtons() {
    logDebug("getModelNumberOfButtons called")
    ["41733" : 8]
}

def installed() {
    state.clear()
    unschedule()
    device.updateSetting("logEnable", [value: "true", type: "bool"])
    device.updateSetting("txtEnable", [value: "true", type: "bool"])
    device.updateSetting("createChildDevices", [value: "false", type: "bool"])
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
    log.warn("create child devices is: ${createChildDevices == true}")
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
    def cmds = [
                zwave.wakeUpV2.wakeUpIntervalGet(),
                zwave.wakeUpV2.wakeUpIntervalSet(seconds: 28800, nodeid: zwaveHubNodeId),
                zwave.wakeUpV2.wakeUpIntervalGet(),
                zwave.wakeUpV2.wakeUpNoMoreInformation()
                ]
    logDebug("returns cmds: ${cmds}")                
    return cmds
}

def refreshBattery() {
    logDebug("refreshBattery called")
    def cmds = zwave.batteryV1.batteryGet()    
    logDebug("returns cmds: ${cmds}")
    return cmds
}

def configureDevice() {
    logDebug("configureDevice called doConfigure: ${doConfigure}")    
    if (doConfigure) {
        def cmds = checkAndSetAssociations(2) + wakeUpConfig()
        device.updateSetting("doConfigure", [value: "false", type: "bool"])           
        doZWaveCommand(cmds)
    }
    else {
        refreshDevice()
    }
}

def refreshDevice() {
    logDebug("refreshDevice called")    
    def cmds = [refreshBattery(), zwave.wakeUpV2.wakeUpNoMoreInformation()]
       doZWaveCommand(cmds)    
}

def associationsAddGroupTwo(BigDecimal nodeID) {
    logDebug("associationsAddGroupTwo called")
    logDebug("got nodeID: ${nodeID}")    
    addAssociation(2, nodeID)
}

def associationsRemoveGroupTwo(BigDecimal nodeID) {
    logDebug("associationsRemoveGroupTwo called")
    logDebug("got nodeID: ${nodeID}")    
    removeAssociation(2, nodeID)
}

def associationsClearGroupTwo() {
    logDebug("associationsClearGroupTwo called")
    clearAssociations(2)
}

def associationsReadGroupTwo() {
    logDebug("associationsReadGroupTwo called")
    readAssociations(2)
}

def associationsAddGroupThree(BigDecimal nodeID) {
    logDebug("associationsAddGroupThree called")
    logDebug("got nodeID: ${nodeID}")    
    addAssociation(3, nodeID)
}

def associationsRemoveGroupThree(BigDecimal nodeID) {
    logDebug("associationsRemoveGroupThree called")
    logDebug("got nodeID: ${nodeID}")    
    removeAssociation(3, nodeID)
}

def associationsClearGroupThree() {
    logDebug("associationsClearGroupThree called")
    clearAssociations(3)
}

def associationsReadGroupThree() {
    logDebug("associationsReadGroupThree called")
    readAssociations(3)
}

def associationsAddGroupFour(BigDecimal nodeID) {
    logDebug("associationsAddGroupFour called")
    logDebug("got nodeID: ${nodeID}")    
    addAssociation(4, nodeID)
}

def associationsRemoveGroupFour(BigDecimal nodeID) {
    logDebug("associationsRemoveGroupFour called")
    logDebug("got nodeID: ${nodeID}")    
    removeAssociation(4, nodeID)
}

def associationsClearGroupFour() {
    logDebug("associationsClearGroupFour called")
    clearAssociations(4)
}

def associationsReadGroupFour() {
    logDebug("associationsReadGroupFour called")
    readAssociations(4)
}

def associationsAddGroupFive(BigDecimal nodeID) {
    logDebug("associationsAddGroupFive called")
    logDebug("got nodeID: ${nodeID}")    
    addAssociation(5, nodeID)
}

def associationsRemoveGroupFive(BigDecimal nodeID) {
    logDebug("associationsRemoveGroupFive called")
    logDebug("got nodeID: ${nodeID}")    
    removeAssociation(5, nodeID)
}

def associationsClearGroupFive() {
    logDebug("associationsClearGroupFive called")
    clearAssociations(5)
}

def associationsReadGroupFive() {
    logDebug("associationsReadGroupFive called")
    readAssociations(5)
}

def addAssociation(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("addAssociation called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    if (checkCurrentAssociationCount(groupID)) {
        storeTargetAssociation(groupID, nodeID)
        def cmds = checkAndSetAssociations(groupID)
        log.warn("battery powered device, wake it up manually to allow the association to be added, alternatively enable the 'Carry out device configuration when it next wakes up' setting!")
        doZWaveCommand(cmds)
    }
    else {
        logDebug("maximum number of Assoiciations for this group has been reached, can't add more Associations!")
    }
}

def removeAssociation(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("removeAssociation called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    removeTargetAssociation(groupID, nodeID)
    def cmds = checkAndSetAssociations(groupID)
       log.warn("battery powered device, wake it up manually to allow the association to be removed, alternatively enable the 'Carry out device configuration when it next wakes up' setting!")
    doZWaveCommand(cmds)
}

def clearAssociations(BigDecimal groupID) {
    logDebug("clearAssociations called")
    logDebug("got groupID: ${groupID}")    
    clearTargetAssociations(groupID)
    def cmds = checkAndSetAssociations(groupID)
       log.warn("battery powered device, wake it up manually to allow associations to be cleared, alternatively enable the 'Carry out device configuration when it next wakes up' setting!")
    doZWaveCommand(cmds)
}

def readAssociations(BigDecimal groupID) {
    logDebug("readAssociations called")
    logDebug("got groupID: ${groupID}")    
    def cmds = zwave.associationV1.associationGet(groupingIdentifier: groupID)
       log.warn("battery powered device, wake it up manually to allow associations to be read, alternatively enable the 'Carry out device configuration when it next wakes up' setting!")
    doZWaveCommand(cmds)
}

def storeCurrentAssociation(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("storeCurrentAssociation called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    def associationGroup = "associationGroup${groupID}Current"
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("current associations: ${associations}")
    String stateNodeID = nodeID.toString()
    if (associations.contains(stateNodeID) == false) {
        associations.add(stateNodeID)
        state[associationGroup] = associations
    }
    logDebug("new current associations: ${associations}")
}

def storeCurrentAssociations(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("storeCurrentAssociations called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    storeCurrentAssociation(groupID, nodeID)
}

def storeCurrentAssociations(BigDecimal groupID, List nodeIDs) {
    logDebug("storeCurrentAssociations called")
    logDebug("got groupID: ${groupID} nodeIDs: ${nodeIDs}")    
    if (nodeIDs != '' && nodeIDs != null && nodeIDs != []) {
        nodeIDs.each { storeCurrentAssociation(groupID, it) }
    }
    else {
        clearCurrentAssociations(groupID)
    }
}

def removeCurrentAssociation(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("removeCurrentAssociation called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    def associationGroup = "associationGroup${groupID}Current"
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("current associations: ${associations}")
    String stateNodeID = nodeID.toString()
    if (associations.contains(stateNodeID) == true) {
        state[associationGroup] = associations.minus([stateNodeID])
    }
    associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("new current associations: ${associations}")
}

def clearCurrentAssociations(BigDecimal groupID) {
    logDebug("clearCurrentAssociations called")
    logDebug("got groupID: ${groupID}")    
    def associationGroup = "associationGroup${groupID}Current"    
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("current associations: ${associations}")
    state[associationGroup] = []
    associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("new current associations: ${associations}")
}

def checkCurrentAssociationCount(BigDecimal groupID) {
    logDebug("checkCurrentAssociationCount called")
    logDebug("got groupID: ${groupID}")    
    def associationGroup = "associationGroup${groupID}Current"
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("current associations: ${associations}")
    return (associations.size() < 5)
}

def storeTargetAssociation(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("storeTargetAssociation called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    def associationGroup = "associationGroup${groupID}Target"
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("target associations: ${associations}")
    String stateNodeID = nodeID.toString()
    if (associations.contains(stateNodeID) == false) {
        associations.add(stateNodeID)
        state[associationGroup] = associations
    }
    associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("new target associations: ${associations}")
}

def removeTargetAssociation(BigDecimal groupID, BigDecimal nodeID) {
    logDebug("removeTargetAssociation called")
    logDebug("got groupID: ${groupID} nodeID: ${nodeID}")    
    def associationGroup = "associationGroup${groupID}Target"
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []    
    logDebug("target associations: ${associations}")
    String stateNodeID = nodeID.toString()
    if (associations.contains(stateNodeID) == true) {
        state[associationGroup] = associations.minus([stateNodeID])
    }
    associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("new target associations: ${associations}")
}

def clearTargetAssociations(BigDecimal groupID) {
    logDebug("clearTargetAssociations called")
    logDebug("got groupID: ${groupID}")    
    def associationGroup = "associationGroup${groupID}Target"    
    logDebug("checking: ${associationGroup}")
    def associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("target associations: ${associations}")
    state[associationGroup] = []
    associations = state[associationGroup] ? state[associationGroup] : []
    logDebug("new target associations: ${associations}")
}

def checkAndSetAssociations(BigDecimal groupID) {
    logDebug("checkAndSetAssociations called")
    def associationGroupTarget = "associationGroup${groupID}Target"
    logDebug("checking: ${associationGroupTarget}")
    def associationsTarget = state[associationGroupTarget] ? state[associationGroupTarget] : []    
    logDebug("target associations: ${associationsTarget}")
    def associationGroupCurrent = "associationGroup${groupID}Current"    
    logDebug("checking: ${associationGroupCurrent}")
    def associationsCurrent = state[associationGroupCurrent] ? state[associationGroupCurrent] : []    
    logDebug("current associations: ${associationsCurrent}")
    def cmds = []    
    if (associationsTarget == []) {
        logDebug("no target associations for this grroup, attempting to remove all!")
        cmds += [
                zwave.associationV2.associationRemove(groupingIdentifier: groupID),
                zwave.associationV1.associationGet(groupingIdentifier: groupID)
                ]
    }
    else {
        associationsTarget.each { 
            if (associationsCurrent.contains(it) == false) {
                logDebug("current associations missing this target association: ${it}, attempting to set it!")
                cmds += [
                        zwave.associationV2.associationSet(groupingIdentifier: groupID, nodeId: new BigDecimal(it)),
                        zwave.associationV1.associationGet(groupingIdentifier: groupID)
                        ]
            }
        }
        associationsCurrent.each { 
            if (associationsTarget.contains(it) == false) {
                logDebug("target associations missing this current association: ${it}, attempting to remove it!")
                cmds += [
                        zwave.associationV2.associationRemove(groupingIdentifier: groupID, nodeId: new BigDecimal(it)),
                        zwave.associationV1.associationGet(groupingIdentifier: groupID)
                        ]
            }
        }
    }
    logDebug("returns cmds: ${cmds}")
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
    doZWaveCommand(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0))
}

def zwaveEvent(hubitat.zwave.commands.associationv1.AssociationReport cmd) {
    logDebug("zwaveEvent hubitat.zwave.commands.associationv1.AssociationReport called")
    logDebug("got cmd: ${cmd}")
    def groupID = cmd.groupingIdentifier
    def nodeIDs = cmd.nodeId
    logDebug("got groupID: ${groupID} nodes: ${nodeIDs}")
    if (groupID != null && groupID != '' && groupID != 1) {
        storeCurrentAssociations(groupID, nodeIDs)
    }
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
        def epNumber = ((button / 2) + 0.5).toInteger()
        if (createChildDevices) {                        
            logDebug("creating child device events")
            logDebug("button: ${button} epNumber: ${epNumber}")
            if (action == "pushed") {
                logDebug("sending Switch event to child device")
                String onOffState = "on"
                if (button % 2 == 0) {
                    onOffState = "off"
                }
                sendEventToChildDevice("EP${epNumber}", "switch", onOffState, "was turned ${onOffState}")
            }
            else if (action == "held") {
                logDebug("starting SwitchLevel event processing for child device")
                String direction = "up"
                if (button % 2 == 0) {
                    direction = "down"
                }
                def currentValue = getChildDeviceCurrentValue("EP${epNumber}", "level") ?: 0
                if (direction == "down" && currentValue == 0) {
                    logDebug("down command but device is at 0%, skipping!")
                }
                else if (direction == "up" && currentValue == 100) {
                    logDebug("up command but device is at 100%, skipping!")
                }
                else {    
                    sendEventToChildDevice("EP${epNumber}", "startLevelChange", direction, "was set to ${direction}")
                }
            }
            else if (action == "released") {
                logDebug("stopping SwitchLevel event processing for child device")
                sendEventToChildDevice("EP${epNumber}", "stopLevelChange", "stop", "was set to stop")
            }
        }
        else {
            def childDevice = this.getChildDevice("${device.id}-EP${epNumber}")
            if (childDevice) {
                logDebug("deleting child device ${device.id}-EP${epNumber}")
                this.deleteChildDevice("${device.id}-EP${epNumber}")
            }
        }
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

def sendEventToChildDevice(address, event, attributeValue, childDescriptionText) {
    logDebug("sendEventToChildDevice called address: ${address} event: ${event} attributeValue: ${attributeValue} descriptionText: ${childDescriptionText}")    
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {    
        logDebug("creating child device for address: ${address}")            
        this.addChildDevice("Vesternet", "Vesternet Z-Wave Wall Controllers & Remote Controls Child Switch", "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true]) 
        childDevice = this.getChildDevice("${device.id}-${address}")
    }            
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, sending event")         
        descriptionText = "${childDevice.displayName} ${childDescriptionText}"
        def childEvent = [name: event, value: attributeValue, descriptionText: descriptionText, type: "physical"]               
        childDevice.parse([getEvent(childEvent)])        
    }
    else {
        log.warn("could not find child device, skipping event!")            
    }
}

def getChildDeviceCurrentValue(address, attribute) {
    logDebug("getChildDeviceCurrentValue called address: ${address} attribute: ${attribute}")  
    def currentValue = "unknown"  
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {    
        logDebug("creating child device for address: ${address}")            
        this.addChildDevice("Vesternet", "Vesternet Z-Wave Wall Controllers & Remote Controls Child Switch", "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true]) 
        childDevice = this.getChildDevice("${device.id}-${address}")
    }            
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, getting state")         
            currentValue = childDevice.currentValue(attribute) ?: "unknown"
            logDebug("got currentValue: ${currentValue}")  
    }
    else {
        log.warn("could not find child device!")            
    }
    return currentValue
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

def commands(java.util.ArrayList cmds, delay) {    
    return delayBetween(cmds.collect { secure(it) }, delay)
}

def commands(java.util.ArrayList cmds) {    
    return cmds.collect { secure(it) }
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

def doZWaveCommand(hubitat.zwave.Command cmd) {
    logDebug("doZWaveCommand(hubitat.zwave.Command cmd) called cmd: ${cmd}")
    logDebug("sending ${cmd}")
    if (cmd) {
        sendHubCommand(new hubitat.device.HubAction(commands(cmd), hubitat.device.Protocol.ZWAVE))
    }
}

def doZWaveCommand(String cmd) {
    logDebug("doZWaveCommand(String cmd) called cmd: ${cmd}")
    logDebug("sending ${cmd}")
    if (cmd) {
        sendHubCommand(new hubitat.device.HubAction(commands(cmd), hubitat.device.Protocol.ZWAVE))
    }
}

def doZWaveCommand(java.util.ArrayList cmds, delay = 1000) {
    logDebug("doZWaveCommand(java.util.ArrayList cmds) called cmds: ${cmds} delay: ${delay}")
    logDebug("sending ${cmds}")
    if (cmds) {
        sendHubCommand(new hubitat.device.HubMultiAction(commands(cmds, delay), hubitat.device.Protocol.ZWAVE))
    }
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