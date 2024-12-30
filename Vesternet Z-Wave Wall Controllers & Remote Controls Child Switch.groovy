/**
 *    Vesternet Z-Wave Wall Controllers & Remote Controls Child Switch
 * 
 */
metadata {
    definition (name: "Vesternet Z-Wave Wall Controllers & Remote Controls Child Switch", namespace: "Vesternet", author: "Vesternet", component: true, singleThreaded: true, importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20Z-Wave%20Wall%20Controllers%20%26%20Remote%20Controls%20Child%20Switch.groovy") {
        capability "Switch"        
        capability "SwitchLevel"        
        capability "Actuator"       
    }
    preferences {        
        input name: "percentageDimming", type: "enum", title: "Level Percent To Increment / Decrement Automatically (%)", description: "Level events are repeated by a timer after receiving a 'dim up' or 'dim down' command until receiving a 'stop dimming' command.  Set the percentage of increase or decrease on each event below.", options: ["1": "1%", "2": "2%", "3": "3%", "4": "4%", "5": "5%", "6": "6%", "7": "7%", "8": "8%", "9": "9%", "10": "10%"], defaultValue: "5"
        input name: "timeDimming", type: "enum", title: "Level Event Repeat Time (ms)", description: "Level events are repeated by a timer after receiving a 'dim up' or 'dim down' command until receiving a 'stop dimming' command.  Set the timer value below.", options: ["250": "250ms", "300": "300ms", "350": "350ms", "400": "400ms", "450": "450ms", "500": "500ms", "550": "550ms", "600": "600ms", "650": "650ms", "700": "700ms", "750": "750ms", "800": "800ms", "850": "850ms", "900": "900ms", "950": "950ms", "1000": "1000ms"], defaultValue: "500"
        input name: "levelMatchSwitch", type: "bool", title: "Raise Level Event To Match Switch Event", description: "Set this to raise a level event when a switch event is raised.  This will raise a 100% level event when an 'on' switch event is raised and a 0% level event when an 'off' switch event is raised.", defaultValue: true
        input name: "switchMatchLevel", type: "bool", title: "Raise Switch Event To Match Level Event", description: "Set this to raise a switch event when a level event is raised.  This will raise an 'on' switch event when a 1% - 99% level event is raised and an 'off' switch event when a 0% level event is raised.", defaultValue: true
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
    }
}

def installed() {
    device.updateSetting("logEnable", [value: "true", type: "bool"])
    device.updateSetting("txtEnable", [value: "true", type: "bool"])
    logDebug("installed called")    
    device.updateSetting("percentageDimming", [value: "5", type: "enum"])
    device.updateSetting("timeDimming", [value: "500", type: "enum"])
    device.updateSetting("levelMatchSwitch", [value: "true", type: "bool"])
    device.updateSetting("switchMatchLevel", [value: "true", type: "bool"])
    runIn(1800,logsOff)
}

def updated() {
    logDebug("updated called")
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("level percent to increment / decrement automatically is: ${percentageDimming}%") 
    log.warn("level event repeat time is: ${timeDimming}ms") 
    log.warn("raise level event to match level switch is: ${levelMatchSwitch == true}")
    log.warn("raise switch event to match level event is: ${switchMatchLevel == true}")
    state.clear()
    unschedule()
    if (logEnable) runIn(1800,logsOff)
}

def on() {
    logDebug("on called")    
    logDebug("no operation, Switch Capability present for Events only!")  
}

def off() {
    logDebug("off called")
    logDebug("no operation, Switch Capability present for Events only!")  
}

def setLevel(level, duration = 1) {
    logDebug("setLevel called")
    logDebug("no operation, SwitchLevel Capability present for Events only!") 
}

void parse(String description) {
    logDebug("parse called")
    log.warn "parse(String description) not implemented"   
}

void parse(List description) {
    logDebug("parse(List description) called")
    description.each {
        if (it.name in ["switch"]) {
            logDebug("got switch event")
            levelStop()
            logText(it.descriptionText)
            sendEvent(getEvent(it))
            if (levelMatchSwitch) {
                logDebug("levelMatchSwitch is true, raising level event")
                def levelValue = 100
                if (it.value == "off") {
                    levelValue = 0
                }
                def levelEvent = [name: "level", value: levelValue, descriptionText: "${device.displayName} was set to ${levelValue}%", type: "physical"] 
                sendEvent(getEvent(levelEvent))
            }            
        }
        else if (it.name in ["startLevelChange"]) {
            logDebug("got startLevelChange event")
            logText(it.descriptionText)            
            def level = percentageDimming ? percentageDimming.toInteger() : 5
            def delay = timeDimming ? timeDimming.toInteger() : 500
            state["levelChangeRunning"] = true
            runInMillis(0, levelChange, [data: ["direction": it.value, "level": level, "delay": delay]])            
        }
        else if (it.name in ["stopLevelChange"]) {
            logDebug("got stopLevelChange event")
            logText(it.descriptionText)
            levelStop()
        }
    }
}    

def levelChange(Map data) {
    logDebug("levelChange called data: ${data}")
    def levelChangeRunning = state["levelChangeRunning"]
    if (levelChangeRunning) {
        logDebug("levelChangeRunning: ${levelChangeRunning}, starting!")
        def currentLevelValue = device.currentValue("level") ?: 0
        def levelValue = 9999
        if (data.direction == "down" && currentLevelValue == 0) {
            logDebug("down command but device is at 0%, skipping!")
        }
        else if (data.direction == "up" && currentLevelValue == 100) {
            logDebug("up command but device is at 100%, skipping!")
        }
        else if (data.direction == "up") {
            levelValue = currentLevelValue + data.level
        }
        else if (data.direction == "down") {
            levelValue = currentLevelValue - data.level
        }
        if (levelValue != 9999) {        
            if (switchMatchLevel) {
                logDebug("switchMatchLevel is true, raising switch event")
                def switchValue = "on"
                if (levelValue == 0) {
                    switchValue = "off"
                }
                def switchEvent = [name: "switch", value: switchValue, descriptionText: "${device.displayName} was turned ${switchValue}", type: "physical"] 
                sendEvent(getEvent(switchEvent))
            }
            def levelEvent = [name: "level", value: levelValue, descriptionText: "${device.displayName} was set to ${levelValue}%", type: "physical"] 
            sendEvent(getEvent(levelEvent))        
            runInMillis(data.delay, levelChange, [data: ["direction": data.direction, "level": data.level, "delay": data.delay]])
        }
    }
    else {
        logDebug("levelChangeRunning: ${levelChangeRunning}, stopping!")
    }
}

def levelStop() {
    logDebug("levelStop called")
    state["levelChangeRunning"] = false
    unschedule("levelChange")
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

def logText(msg) {
    if (txtEnable != false) {
        log.info("${msg}")
    }
}

def logsOff() {
    log.warn("debug logging disabled")
    device.updateSetting("logEnable", [value:"false", type: "bool"])
}