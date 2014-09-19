package com.cameronsystems.fix.universalserver.test;

import com.cameronsystems.fix.message.IFIXMessage;
import com.cameronsystems.fix.processor.FIXSourceListenerBase;
import com.cameronsystems.util.dataactive.AttributeUtility;
import com.cameronsystems.util.logger.ILogger;
import com.cameronsystems.util.logger.LoggerManager;
import com.cameronsystems.util.messaging.MessageEvent;
import com.orcsoftware.util.stats.PerformanceMeter;

public class TDPerformanceMessageTester extends FIXSourceListenerBase
{
  public static final String ATTRIBUTE_CYCLES = "cycles";
  public static final String ATTRIBUTE_STORES_MESSAGES = "storesMessages";
  public static final int DEFAULT_CYCLES = 10000;
  private static final ILogger a = LoggerManager.getLogger(PerformanceMessageTester.class);
  protected String id_;
  protected long cycles_;
  private PerformanceMeter b;
  private PerformanceMeter c;
  private long d;
  private PerformanceMeter e;
  private PerformanceMeter f;
  private long g;
  private boolean h;

  public TDPerformanceMessageTester()
  {
    this.b = new PerformanceMeter();
    this.c = new PerformanceMeter();
    this.e = new PerformanceMeter();
    this.f = new PerformanceMeter();
  }

  public boolean openComponent()
    throws Exception
  {
    if (isComponentOpen()) {
      return true;
    }

    this.id_ = ((String)super.getAttribute("id"));
    if ((this.id_ == null) || (this.id_.length() == 0)) {
      throw new Exception("Attribute must be set. attribute: id");
    }

    String str = (String)getAttribute("cycles");
    if ((str == null) || (str.length() == 0))
      this.cycles_ = 10000L;
    try
    {
      this.cycles_ = Integer.parseInt(str);
    } catch (Exception localException) {
      throw new Exception("Incorrect value for attribute cycles. Expected an integer. found: " + str);
    }

    this.h = AttributeUtility.getBooleanAttribute("storesMessages", this, false);
    setStoresMessages(this.h);

    return super.openComponent();
  }

  public boolean closeComponent()
    throws Exception
  {
    if (!isComponentOpen()) {
      return true;
    }
    this.d = 0L;
    this.g = 0L;
    return super.closeComponent();
  }

  public void onMessageToFix(MessageEvent paramMessageEvent, IFIXMessage paramIFIXMessage)
  {
    if (this.g == 0L) {
      this.e.resetTimer();
      this.f.resetTimer();
    } else if (this.g % this.cycles_ == 0L) {
      StringBuffer localStringBuffer = new StringBuffer(this.id_);
      localStringBuffer.append(": (Outbound) Last ");
      localStringBuffer.append(this.cycles_);
      localStringBuffer.append(" messages- TPS: ");
      localStringBuffer.append(Math.round(this.f.getTPS(this.cycles_) * 100.0D) / 100.0D);
      localStringBuffer.append(" duration: ");
      localStringBuffer.append(this.f.getLastDuration());
      localStringBuffer.append("ms");
      a.info(localStringBuffer.toString());
      localStringBuffer = new StringBuffer(this.id_);
      localStringBuffer.append(": (Outbound) Total ");
      localStringBuffer.append(this.g);
      localStringBuffer.append(" messages- TPS: ");
      localStringBuffer.append(Math.round(this.e.getTPS(this.g) * 100.0D) / 100.0D);
      localStringBuffer.append(" duration: ");
      localStringBuffer.append(this.e.getLastDuration());
      localStringBuffer.append("ms");
      a.info(localStringBuffer.toString());
      this.f.resetTimer();
    }
    this.g += 1L;
  }

  public void onMessageFromFix(MessageEvent paramMessageEvent, IFIXMessage paramIFIXMessage)
  {
    if (this.d == 0L) {
      this.b.resetTimer();
      this.c.resetTimer();
    } else if (this.d % this.cycles_ == 0L) {
      StringBuffer localStringBuffer = new StringBuffer(this.id_);
      localStringBuffer.append(": (Inbound) Last ");
      localStringBuffer.append(this.cycles_);
      localStringBuffer.append(" messages- TPS: ");
      localStringBuffer.append(Math.round(this.c.getTPS(this.cycles_) * 100.0D) / 100.0D);
      localStringBuffer.append(" duration: ");
      localStringBuffer.append(this.c.getLastDuration());
      localStringBuffer.append("ms");
      a.info(localStringBuffer.toString());
      localStringBuffer = new StringBuffer(this.id_);
      localStringBuffer.append(": (Inbound) Total ");
      localStringBuffer.append(this.d);
      localStringBuffer.append(" messages- TPS: ");
      localStringBuffer.append(Math.round(this.b.getTPS(this.d) * 100.0D) / 100.0D);
      localStringBuffer.append(" duration: ");
      localStringBuffer.append(this.b.getLastDuration());
      localStringBuffer.append("ms");
      a.info(localStringBuffer.toString());
      this.c.resetTimer();
    }
    this.d += 1L;
  }
}