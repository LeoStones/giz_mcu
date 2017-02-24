#include "stm32f10x.h"
#include <stm32f10x_adc.h>
#include "stm32f10x_dma.h"
#include "stm32f10x_rcc.h"
#include "hal_adc_dma.h"
__IO uint16_t ADC_ConvertedValue;

#define ADC1_DR_Address ((u32)0x40012400+0x4c)
/**********************************************************
*
*   ≈‰÷√DMA1
*   add by alex.lin --2014-12-5
*
***********************************************************/
static void ADC1_GPIO_Config(void)
{
    GPIO_InitTypeDef GPIO_InitStructure; 
    /* Enable DMA clock */ 
    RCC_AHBPeriphClockCmd(RCC_AHBPeriph_DMA1, ENABLE);
    /* Enable ADC1 and GPIOC clock */ 
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_ADC1 | RCC_APB2Periph_GPIOA, ENABLE);
    /* Configure PC.01 as analog input */  
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_5;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN; 
    GPIO_Init(GPIOC, &GPIO_InitStructure); 
}
/**********************************************************
*
*   ≈‰÷√DMA1
*   add by alex.lin --2014-12-5
*
***********************************************************/
void  DMA1_Config(void)
{
    DMA_InitTypeDef DMA_InitStructure;
    DMA_DeInit(DMA1_Channel1);
    DMA_InitStructure.DMA_PeripheralBaseAddr = ADC1_DR_Address;//ADC1_DR_Address;
    DMA_InitStructure.DMA_MemoryBaseAddr = (u32)&ADC_ConvertedValue;
    DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
    DMA_InitStructure.DMA_BufferSize = 1;  
    DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
    DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Disable;// 
    DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_HalfWord;
    DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_HalfWord;
    DMA_InitStructure.DMA_Mode = DMA_Mode_Circular;
    DMA_InitStructure.DMA_Priority = DMA_Priority_High;
    DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
    DMA_Init(DMA1_Channel1, &DMA_InitStructure);
    DMA_Cmd(DMA1_Channel1, ENABLE);
}

/**********************************************************
*
*   ≈‰÷√DMA1
*   add by alex.lin --2014-12-5
*
***********************************************************/
static void ADC1_Mode_Config(void)
{
    ADC_InitTypeDef ADC_InitStructure;
    DMA1_Config();
    ADC_InitStructure.ADC_Mode = ADC_Mode_Independent;
    ADC_InitStructure.ADC_ScanConvMode = DISABLE ;
    ADC_InitStructure.ADC_ContinuousConvMode = ENABLE;
    ADC_InitStructure.ADC_ExternalTrigConv = ADC_ExternalTrigConv_None;
    ADC_InitStructure.ADC_DataAlign = ADC_DataAlign_Right;
    ADC_InitStructure.ADC_NbrOfChannel = 1;
    ADC_Init(ADC1, &ADC_InitStructure);
    RCC_ADCCLKConfig(RCC_PCLK2_Div8);
    //…Ë÷√≤…—˘÷‹∆⁄
    ADC_RegularChannelConfig(ADC1, ADC_Channel_5, 1, ADC_SampleTime_55Cycles5);
    ADC_DMACmd(ADC1, ENABLE);       
    ADC_Cmd(ADC1, ENABLE);
    ADC_ResetCalibration(ADC1);
    while(ADC_GetResetCalibrationStatus(ADC1));
    ADC_StartCalibration(ADC1);
    while(ADC_GetCalibrationStatus(ADC1));
    ADC_SoftwareStartConvCmd(ADC1, ENABLE);
}
/**********************************************************
*
*   ≈‰÷√DMA1
*   add by alex.lin --2014-12-5
*
***********************************************************/
void ADC1_Init(void)
{
    ADC1_GPIO_Config();
    ADC1_Mode_Config();
}
