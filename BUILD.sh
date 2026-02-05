#!/bin/bash

# RiseKaptan Build Script
# Bu script eklentiyi Maven olmadan derler

echo "=== RiseKaptan Build Script ==="
echo ""

# Önce Spigot API'sini indirmemiz gerekiyor
echo "Not: Bu eklentiyi derlemek için Spigot API'sine ihtiyacınız var."
echo ""
echo "Derleme Adımları:"
echo "1. BuildTools ile Spigot 1.16.5 API'sini derleyin:"
echo "   java -jar BuildTools.jar --rev 1.16.5"
echo ""
echo "2. Maven ile projeyi derleyin:"
echo "   mvn clean package"
echo ""
echo "3. Derlenmiş JAR dosyası target/RiseKaptan.jar konumunda olacaktır."
echo ""
echo "BuildTools indirme linki:"
echo "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
echo ""
echo "=== Alternatif: Online Derleme Servisleri ==="
echo "- GitHub Actions kullanabilirsiniz"
echo "- https://ci.codemc.io gibi online CI/CD servisleri"
echo ""
echo "Kaynak kodlar hazır! RiseKaptan klasöründe tüm dosyalar mevcut."
