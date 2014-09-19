package com.cameronsystems.fix.routing;

import java.util.StringTokenizer;

import com.cameronsystems.fix.IConnectionPoint;
import com.cameronsystems.fix.IParty;
import com.cameronsystems.fix.Session;
import com.cameronsystems.fix.configuration.Constants;
import com.cameronsystems.fix.manager.ISessionManager;
import com.cameronsystems.fix.message.IFIXMessage;
import com.cameronsystems.fix.processor.FIXSourceBase;
import com.cameronsystems.fix.util.FIXApplVerID;
import com.cameronsystems.util.dataactive.AttributeUtility;
import com.cameronsystems.util.logger.ILogger;
import com.cameronsystems.util.logger.LoggerManager;

public class RoutingExampleSource extends FIXSourceBase
  implements Runnable
{
  public static final String INTERVAL = "interval";
  public static final String ATTRIBUTE_SIDE = "side";
  public static final String ATTRIBUTE_ON_BEHALF_OF = "onBehalfOf";
  public static final String ATTRIBUTE_DELIVER_TO = "deliverTo";
  public static final String SIDE_BUY = "buy";
  public static final String SIDE_SELL = "sell";
  private static final ILogger a = LoggerManager.getLogger(RoutingExampleSource.class);

  private int b = 5000;
  private boolean c;
  private String d;
  private int e;
  private String[] f;
  private String g;
  private String[] h = { "5.0", "4.2", "4.3", "4.4" };
  private volatile boolean i;
  private int j;

  public RoutingExampleSource()
  {
    if (a.isDebugEnabled())
      a.debug("<init>() called");
  }

  public boolean openComponent()
    throws Exception
  {
    if (a.isDebugEnabled()) {
      a.debug("openComponent() called");
    }

    if (isComponentOpen()) {
      return true;
    }

    setStoresMessages(false);

    refreshComponent();
    return super.openComponent();
  }

  public void refreshComponent() throws Exception
  {
    String str1 = (String)getAttribute("interval");
    try {
      this.b = Integer.parseInt(str1);
    }
    catch (Exception localException)
    {
    }
    this.c = "buy".equals(AttributeUtility.getStringAttribute("side", this, null));
    this.d = AttributeUtility.getStringAttribute("onBehalfOf", this, null);
    String str2 = AttributeUtility.getStringAttribute("deliverTo", this, null);
    StringTokenizer localStringTokenizer = new StringTokenizer(str2);
    this.e = localStringTokenizer.countTokens();
    this.f = new String[this.e];
    int k = 0;
    while (localStringTokenizer.hasMoreTokens()) {
      this.f[k] = localStringTokenizer.nextToken();
      k++;
    }

    this.g = getFIXTransportVersion(getSessionManager());
  }

  public boolean setComponentActive(boolean paramBoolean)
    throws Exception
  {
    if (a.isDebugEnabled()) {
      a.debug("setComponent(" + paramBoolean + ") called");
    }

    if (paramBoolean == isComponentActive()) {
      return paramBoolean;
    }

    if (paramBoolean) {
      this.i = true;
      new Thread(this).start();
    } else {
      this.i = false;
    }

    return super.setComponentActive(paramBoolean);
  }

  public void run()
  {
    if (a.isDebugEnabled()) {
      a.debug("run() called");
    }

    while (this.i) {
      Session localSession = getSessionManager().getSession();
      if (localSession != null)
        if (localSession.isLoggedOn())
          try {
            fireMessage(getMessage());
            if (this.b > 0)
              try {
                Thread.sleep(this.b);
              } catch (InterruptedException localInterruptedException1) {
                a.warn("Send thread sleep interrupted.", localInterruptedException1);
              }
          }
          catch (Exception localException) {
            a.error("Error sending message.", localException);
          }
        else
          try {
            Thread.sleep(5000L);
          } catch (InterruptedException localInterruptedException2) {
            a.warn("Send thread sleep interrupted.", localInterruptedException2);
          }
    }
  }

  private String getFIXTransportVersion(ISessionManager paramISessionManager)
  {
    IConnectionPoint localIConnectionPoint = paramISessionManager.getConnectionPoint();
    String str = null;
    if (localIConnectionPoint != null) {
      IParty localIParty = localIConnectionPoint.getOurParty();
      if (localIParty != null) str = (String)localIParty.getAttribute("transportVersion");
    }
    return str;
  }

  private IFIXMessage getMessage()
  {
    if (a.isDebugEnabled())
      a.debug("getMessage() called");
    IFIXMessage localIFIXMessage;
    if (this.c) {
      localIFIXMessage = getToFIXMessageFactory().createFIXMessage("D");
    } else {
      localIFIXMessage = getToFIXMessageFactory().createFIXMessage("8");
      localIFIXMessage.setValue(32, 0);
      localIFIXMessage.setValue(31, 0);
      localIFIXMessage.setValue(39, "0");
    }
    if (this.f != null) {
      localIFIXMessage.setValue(128, this.f[(this.j % this.e)]);
    }
    if (this.d != null) {
      localIFIXMessage.setValue(115, this.d);
    }
    if (this.g != null) {
      String str = FIXApplVerID.getFixApplVerID(this.h[(this.j % this.e % 4)]);
      if (str != null) {
        localIFIXMessage.setValue(1128, str);
      }
    }

    localIFIXMessage.setValue(54, "1");
    double d1 = 12.34D;
    localIFIXMessage.setValue(44, Constants.NUMBERFORMAT.format(d1));
    localIFIXMessage.setValue(38, Math.max(1000, (int)(50.0D * Math.random()) * 1000));
    localIFIXMessage.setValue(55, "XYZ");
    localIFIXMessage.setValue(11, "Order:" + this.j++);
    return localIFIXMessage;
  }
}