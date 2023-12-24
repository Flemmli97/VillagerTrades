# Villager Trades

[![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=Discord)](https://discord.gg/8Cx26tfWNs)

Server side mod to edit villager trades

To use this mod as a dependency add the following snippet to your build.gradle:  
```groovy
repositories {
    maven {
        name = "Flemmli97"
        url "https://gitlab.com/api/v4/projects/21830712/packages/maven"
    }
}

dependencies {    
    //Fabric/Loom==========    
    modImplementation("io.github.flemmli97:villagertrades:${minecraft_version}-${mod_version}-${mod_loader}")
    
    //Forge==========    
    compile fg.deobf("io.github.flemmli97:villagertrades:${minecraft_version}-${mod_version}-${mod_loader}")
}
```
