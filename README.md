## Java11

```
brew tap homebrew/cask-versions
brew cask install java11
```

.bashrc
```
export JAVA_HOME=`/usr/libexec/java_home -v 11`
PATH=${JAVA_HOME}/bin:${PATH}
```

## 環境変数系のローカル準備

* application.propertiesの値はConfig Varsから値を確認し、export key=valueする
* twitter4j
    * Dropboxにあるのでsrc/main/resourcesに置く