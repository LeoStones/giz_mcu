#ifndef __ADC_DMA_H_
#define __ADC_DMA_H_
#include "stm32f10x.h"
//ADC ��ת�����
extern __IO uint16_t ADC_ConvertedValue;
void  DMA1_Config(void);
void ADC1_Init(void);
#endif