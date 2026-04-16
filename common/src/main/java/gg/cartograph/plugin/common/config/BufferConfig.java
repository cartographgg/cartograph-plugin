package gg.cartograph.plugin.common.config;

public class BufferConfig
{

    private int sizeThreshold = 50;

    private int timeThreshold = 60;

    private int maxRetries = 3;

    public int getSizeThreshold()
    {
        return sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold)
    {
        this.sizeThreshold = sizeThreshold;
    }

    public int getTimeThreshold()
    {
        return timeThreshold;
    }

    public void setTimeThreshold(int timeThreshold)
    {
        this.timeThreshold = timeThreshold;
    }

    public int getMaxRetries()
    {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }
}
