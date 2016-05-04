# Bukkit Slack Integration

kotlinified

The original author is [teej107], I just ported it to Gradle and Kotlin. I'm using it for a private MC server.

have fun

uses bukkit 1.9.2, modify the build.gradle if you need a different api

shades all dependencies into jar, no need to manually depend kotlin stdlib in your server config

## usage

    ./gradlew jar
    cp build/libs/* my/server/plugins/
    echo "hey you should probably configure the plugin after running the server"

## disclaimer

no implied warranty, not responsible for damages, yada yada

[teej107]: https://github.com/teej107/SlackIntegration