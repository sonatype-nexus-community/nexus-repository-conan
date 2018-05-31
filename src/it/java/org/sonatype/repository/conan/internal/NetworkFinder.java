package org.sonatype.repository.conan.internal;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.sonatype.goodies.testsupport.TestSupport;

public class NetworkFinder
    extends TestSupport
{
  public String getAddressToUse() {
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

      while(networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();

        if(interestedInInterface(networkInterface)) {
          for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
            if(interfaceAddress.getAddress() instanceof Inet4Address) {
              return interfaceAddress.getAddress().getHostAddress();
            }
          }
        }
      }
    }
    catch (SocketException e) {
      e.printStackTrace();
    }

    return fallbackToLocalhost();
  }

  private String fallbackToLocalhost() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e) {
      log("Unable to fallback to a useable address");
    }
    throw new RuntimeException("Unable to find valid network");
  }

  private boolean interestedInInterface(final NetworkInterface networkInterface) throws SocketException {
    return !(networkInterface.isLoopback() ||
        networkInterface.isPointToPoint() ||
        networkInterface.isVirtual() ||
        !networkInterface.isUp());
  }
}
